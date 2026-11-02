package jpark.bro.days.core.data.mapper

import jpark.bro.days.core.database.entity.ItemEntity
import jpark.bro.days.core.database.entity.ItemReminderEntity
import jpark.bro.days.core.database.entity.SyncState
import jpark.bro.days.core.model.Category
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemReminder
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.RepeatRule
import jpark.bro.days.core.model.TimeSlot
import jpark.bro.days.core.model.Visibility
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** 시각 컬럼 형식 HH:mm (data-model-erd 2장) */
private val TimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** 열거형은 name, 날짜는 yyyy-MM-dd (ISO), 시각은 HH:mm 문자열로 저장한다 (data-model-erd 1장) */
internal fun ItemEntity.toModel(): Item =
    Item(
        id = id,
        content =
            ItemContent(
                type = ItemType.valueOf(type),
                title = title,
                colorKey = colorKey,
                iconKey = iconKey,
                visibility = Visibility.valueOf(visibility),
                coupleId = coupleId,
                category = Category.valueOf(category),
                date = date?.let(LocalDate::parse),
                countMode = countMode?.let(CountMode::valueOf),
                countFirstDayAsOne = countFirstDayAsOne,
                repeatRule = RepeatRule.valueOf(repeatRule),
                timeSlot = timeSlot?.let(TimeSlot::valueOf),
                time = time?.let { LocalTime.parse(it, TimeFormat) },
                place = place,
                memo = memo,
                isDone = isDone,
                showAsDday = showAsDday,
                backgroundImageId = backgroundImageId,
            ),
        ownerUserId = ownerUserId,
        updatedBy = updatedBy,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

/** [serverUpdatedAt] · [deletedAt]은 모델에 없는 Room · 동기화 값이라 호출하는 쪽이 넘긴다 */
internal fun Item.toEntity(
    syncState: SyncState,
    serverUpdatedAt: Long? = null,
    deletedAt: Long? = null,
): ItemEntity =
    ItemEntity(
        id = id,
        type = content.type.name,
        ownerUserId = ownerUserId,
        coupleId = content.coupleId,
        visibility = content.visibility.name,
        title = content.title,
        category = content.category.name,
        date = content.date?.toString(),
        countMode = content.countMode?.name,
        countFirstDayAsOne = content.countFirstDayAsOne,
        repeatRule = content.repeatRule.name,
        timeSlot = content.timeSlot?.name,
        time = content.time?.format(TimeFormat),
        place = content.place,
        memo = content.memo,
        isDone = content.isDone,
        showAsDday = content.showAsDday,
        colorKey = content.colorKey,
        iconKey = content.iconKey,
        backgroundImageId = content.backgroundImageId,
        updatedBy = updatedBy,
        createdAt = createdAt,
        updatedAt = updatedAt,
        serverUpdatedAt = serverUpdatedAt,
        deletedAt = deletedAt,
        syncState = syncState.name,
    )

internal fun ItemReminderEntity.toModel(): ItemReminder =
    ItemReminder(offsetDays = offsetDays, timeOfDay = LocalTime.parse(timeOfDay, TimeFormat))

internal fun ItemReminder.toEntity(
    id: String,
    userId: String,
    itemId: String,
): ItemReminderEntity =
    ItemReminderEntity(
        id = id,
        userId = userId,
        itemId = itemId,
        offsetDays = offsetDays,
        timeOfDay = timeOfDay.format(TimeFormat),
    )
