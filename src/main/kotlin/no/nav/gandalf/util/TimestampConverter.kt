package no.nav.gandalf.util

import java.sql.Timestamp
import java.time.LocalDateTime
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class TimestampConverter : AttributeConverter<LocalDateTime?, Timestamp?> {
    override fun convertToDatabaseColumn(localDateTime: LocalDateTime?): Timestamp? {
        return if (localDateTime == null) null else Timestamp.valueOf(localDateTime)
    }

    override fun convertToEntityAttribute(sqlTimestamp: Timestamp?): LocalDateTime? {
        return sqlTimestamp?.toLocalDateTime()
    }
}
