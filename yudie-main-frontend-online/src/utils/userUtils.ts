/**
 * 获取默认头像
 * @param userName 用户名
 * @returns 默认头像URL
 */
export const getDefaultAvatar = (userName: string) => {
  const defaultName = userName || 'Guest'
  return `https://xiaorui-1350018626.cos.ap-nanjing.myqcloud.com/public/1925200238819598337/2025-05-31_gXMMGtmOoe0Vi9EI.webp`
}
