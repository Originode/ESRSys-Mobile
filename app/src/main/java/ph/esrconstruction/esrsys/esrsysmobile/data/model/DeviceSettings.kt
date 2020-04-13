package ph.esrconstruction.esrsys.esrsysmobile.data.model

import android.os.Build
import androidx.annotation.RequiresApi
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month

import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
open class DeviceSettings: RealmObject() {
    @PrimaryKey
    var TerminalID: String = "Unknown-000"

    var cachedUser:String = ""
    //var LastUpdate: LocalDateTime = LocalDateTime.now()



}