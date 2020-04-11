package ph.esrconstruction.esrsys.esrsysmobile.data.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
open class LoggedInUser: RealmObject() {
    @PrimaryKey
    var userId: String = ""

    var displayName: String =""

    var encodedCredentials: String = ""

    var token: String = ""
}