package no.nav.gandalf.domain

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull

@Entity
@Table(name = "KEYSTORE_LOCK")
data class KeyStoreLock(
    @Id
    @get: NotNull @Column(name = "ID") var id: Long = 0,
    @get: NotNull @Column(name = "LOCKED") var locked: Long = 0
) {

    constructor(id: Long, locked: Boolean) : this() {
        this.id = id
        setLocked(locked)
    }

    fun isLocked(): Boolean {
        return locked > 0
    }

    private fun setLocked(locked: Boolean) {
        this.locked =
                (when {
                    locked -> 1
                    else -> 0
                }).toLong()
    }
}
