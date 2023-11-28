package no.nav.gandalf.accesstoken

interface ClockSkew {
    fun getMaxClockSkew(): Long

    fun setMaxClockSkew(maxClockSkewSeconds: Long?)
}
