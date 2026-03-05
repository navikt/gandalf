package no.nav.gandalf.util

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.sql.Timestamp
import java.time.LocalDateTime

@Converter(autoApply = true)
class TimestampConverter : AttributeConverter<LocalDateTime?, Timestamp?> {
    override fun convertToDatabaseColumn(localDateTime: LocalDateTime?): Timestamp? = if (localDateTime == null) null else Timestamp.valueOf(localDateTime)

    override fun convertToEntityAttribute(sqlTimestamp: Timestamp?): LocalDateTime? = sqlTimestamp?.toLocalDateTime()
}
