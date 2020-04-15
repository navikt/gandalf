package no.nav.gandalf.domain

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "KEYSTORE_LOCK")
class KeyStoreLock {
    @Id
    @Column(name = "ID", updatable = true, nullable = false)
    var id: Long = 0

    @Column(name = "LOCKED", updatable = true, nullable = false)
    private var locked: Long = 0

    fun KeyStoreLock(id: Long, locked: Boolean) {
        this.id = id
        setLocked(locked)
    }

    fun isLocked(): Boolean {
        return locked > 0
    }

    private fun setLocked(locked: Boolean) {
        this.locked = (if (locked) 1 else 0).toLong()
    }
}