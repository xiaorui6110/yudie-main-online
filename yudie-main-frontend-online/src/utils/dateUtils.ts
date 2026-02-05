import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

/**
 * 格式化时间
 * @param date 日期
 * @returns 格式化后的时间字符串
 */
export const formatTime = (date: Date | string | number | undefined): string => {
  if (!date) return ''

  const now = dayjs()
  const target = dayjs(date)
  const diff = now.diff(target, 'day')

  if (diff === 0) {
    // 今天，显示具体时间
    return target.format('HH:mm')
  } else if (diff === 1) {
    // 昨天
    return '昨天 ' + target.format('HH:mm')
  } else if (diff < 7) {
    // 一周内
    return target.format('dddd HH:mm')
  } else if (now.year() === target.year()) {
    // 今年
    return target.format('MM-DD HH:mm')
  } else {
    // 更早
    return target.format('YYYY-MM-DD HH:mm')
  }
}

/**
 * 格式化消息时间
 * @param date 日期
 * @returns 格式化后的时间字符串
 */
export const formatMessageTime = (date: Date | string | number | undefined): string => {
  if (!date) return ''

  const now = dayjs()
  const target = dayjs(date)

  if (target.isSame(now, 'day')) {
    // 当天消息只显示时间
    return target.format('HH:mm')
  } else if (target.isSame(now.subtract(1, 'day'), 'day')) {
    // 昨天的消息显示"昨天"
    return '昨天'
  } else if (target.isSame(now, 'year')) {
    // 今年的消息显示月日
    return target.format('MM-DD')
  } else {
    // 往年的消息显示年月日
    return target.format('YYYY-MM-DD')
  }
}

/**
 * 获取相对时间
 * @param date 日期
 * @returns 相对时间字符串
 */
export const getRelativeTime = (date: Date | string | number): string => {
  return dayjs(date).fromNow()
}
