package ph.esrconstruction.esrsys.esrsysmobile.utils;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Parcelable;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orhanobut.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import ph.esrconstruction.esrsys.esrsysmobile.cards.CardData;
import ph.esrconstruction.esrsys.esrsysmobile.cards.EmployeeCardData;

public class NfcUtils {
    public static boolean bruteAuthenticateSectorWithKeyA(MifareClassic mif, int sectorNumber) throws IOException {
        if (mif.authenticateSectorWithKeyA(sectorNumber, MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY)) {
            return true;
        } else if (mif.authenticateSectorWithKeyA(sectorNumber, MifareClassic.KEY_DEFAULT)) {
            return true;
        } else if (mif.authenticateSectorWithKeyA(sectorNumber,MifareClassic.KEY_NFC_FORUM)) {
            return true;
        } else {
            Logger.d( "Authorization denied for sector " + sectorNumber);
            return false;
        }
    }

    @FunctionalInterface
    public interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }

    public static void readCard(Intent intent, TriConsumer<Boolean,Byte,String> callback) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        byte[] tagId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        MifareClassic mif = MifareClassic.get(tag);
        int ttype = mif.getType();
        int tsize = mif.getSize();
        int s_len = mif.getSectorCount();
        int b_len = mif.getBlockCount();
        int ccd = 0;
        boolean checkHash = false;

        byte[] card_type = new byte[1];
        byte[] digest_bytes_expected = new byte[14];
        byte[] digest_bytes_actual = new byte[14];
        byte[] size_bytes = new byte[1];

        String data = "";
        String info = "";


        try { //read

            mif.connect();
            if (mif.isConnected()){

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );



                int sectors_to_read = s_len-1;
                int blocks_to_read = (sectors_to_read*3)+1;
                for(int i=0; i< sectors_to_read+1; i++){

                    boolean isAuthenticated = bruteAuthenticateSectorWithKeyA(mif,i);

                    if(isAuthenticated && i>0) {
                        for(int j=0; j< mif.getBlockCountInSector(i)-1; j++) {
                            if(ccd >= blocks_to_read)  break;
                            int block_index = mif.sectorToBlock(i)+j;
                            if(i==1 && j==0){ //size block,
                                //mif.writeBlock(block_index, ByteBuffer.allocate(16).order(ByteOrder.nativeOrder()).putInt(sectorstowrite).array());

                                byte[] block = mif.readBlock(block_index);
                                ByteBuffer buffer_block = ByteBuffer.wrap(block);

                                buffer_block.get(size_bytes);
                                buffer_block.get(card_type);
                                buffer_block.get(digest_bytes_expected);
                                Logger.d(Etc.bytesToHexString(buffer_block.array()));

                                blocks_to_read = size_bytes[0];
                                sectors_to_read  = (int) Math.ceil(((float)blocks_to_read+1)/3);
                                Logger.d("reading size block =  " + blocks_to_read + " sectors to read = " + sectors_to_read +  ", card type = " + CardData.byteToCardTypeString(card_type[0]) + " : 0x" + Character.forDigit(card_type[0] & 0x0F, 16));
                                //outputStream.write( block );
                            }else{ //read blocks
                                //Logger.d("reading chunk number " + (ccd+1) + " sector " + i + " block " + j + " index:" + block_index);
                                byte[] block = mif.readBlock(block_index);
                                outputStream.write( block );
                                ccd++;
                            }
                        }
                    }

                }
                mif.close();

                byte c[] = outputStream.toByteArray( );
                data = Etc.bytesToHexString(c);
                info = new String(c).trim();
                //info = info.substring(0,info.lastIndexOf('}')+1); //clean


                Logger.d( data);
                Logger.d( info);

                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(info.getBytes());
                ByteBuffer.wrap(md.digest()).get(digest_bytes_actual);

                checkHash = Arrays.equals(digest_bytes_expected,digest_bytes_actual) && card_type[0] != CardData.CardTypes.UNKNWON;

                String expectedHash = Etc.bytesToHexString(digest_bytes_expected);
                String actualHash = Etc.bytesToHexString(digest_bytes_actual);
                Logger.d("hash:  " + expectedHash + " == " + actualHash + " " + Arrays.equals(digest_bytes_expected,digest_bytes_actual));

                ////
            }
            if(mif.isConnected()) mif.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            Logger.d("read success");
            callback.accept(checkHash,card_type[0],info);
        }

    }


    public static boolean writeEmployeeCardToMifareClassic(Intent intent, EmployeeCardData ec) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        byte[] tagId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        MifareClassic mif = MifareClassic.get(tag);
        int ttype = mif.getType();
        int tsize = mif.getSize();
        boolean res;
        int s_len = mif.getSectorCount();
        int b_len = mif.getBlockCount();
        Logger.d( "MifareClassic tag type: " + ttype + ", size: " + tsize + ", sector count: " + s_len + ", block count: " + b_len);



        Gson gson = Converters.registerDateTime(new GsonBuilder()).create();
        String stringData = gson.toJson(ec);
        Logger.d(stringData);
        Logger.d("data size = " + stringData.getBytes(Charset.forName("US-ASCII")).length);

        byte[][] xdivideArray = Etc.divideArray(stringData.getBytes(Charset.forName("US-ASCII")),16);
        Logger.d("chunks = " + xdivideArray.length);
        int sectorstowrite = (int) Math.ceil(((float)xdivideArray.length+1)/3);
        int ccd = 0;
        Logger.d("sectors to use = " + sectorstowrite);

        try { //write
            mif.connect();
            if (mif.isConnected()){

                for(int i=0; i< sectorstowrite+1; i++){

                    boolean isAuthenticated = bruteAuthenticateSectorWithKeyA(mif,i);


                    if(isAuthenticated && i>0) {
                        for(int j=0; j< mif.getBlockCountInSector(i)-1; j++) {
                            if(ccd >= xdivideArray.length) break;
                            int block_index = mif.sectorToBlock(i)+j;
                            if(i==1 && j==0){ //size and card type block, write number of sectors being used in card, and type of card


                                byte[] size_byte =  {(byte)xdivideArray.length};
                                byte[] digest_bytes = ByteBuffer.allocate(14).array();


                                MessageDigest md = MessageDigest.getInstance("MD5");
                                md.update(stringData.getBytes());

                                ByteBuffer.wrap(md.digest()).get(digest_bytes);
                                String myHash = Etc.bytesToHexString(digest_bytes);
                                Logger.d("hash = " + myHash + ", " + digest_bytes.length);

                                ByteBuffer buff = ByteBuffer.allocate(16);
                                buff.put(size_byte);
                                buff.put(ec.CardType);
                                buff.put(digest_bytes);

                                Logger.d(Etc.bytesToHexString(buff.array()));

                                byte[] combined = buff.array();
                                Logger.d("writing size block =  " + xdivideArray.length + ", card type = " + CardData.byteToCardTypeString(ec.CardType) + " : 0x" + Character.forDigit(ec.CardType & 0x0F, 16));
                                mif.writeBlock(block_index, combined);
                            }else{ //write blocks
                                // Logger.d("writing chunk number " + (ccd+1) + "/"+ xdivideArray.length +" sector " + i + " block " + j + " index:" + block_index);
                                mif.writeBlock(block_index, xdivideArray[ccd]);
                                ccd++;
                            }
                        }
                    }


                }
            }

            mif.close();

        } catch (IOException e) {
            e.printStackTrace();
            res = false;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            res = false;
        } finally {

            Logger.d("write success");
            res = true;
        }
        return res;
    }


    public static NdefMessage[] getNdefMessages(Intent intent) {
        Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMessages != null) {
            NdefMessage[] messages = new NdefMessage[rawMessages.length];
            for (int i = 0; i < messages.length; i++) {
                messages[i] = (NdefMessage) rawMessages[i];
            }
            return messages;
        } else {
            return null;
        }
    }



}
