import request from './request'
export const userApi = {
  uploadAvatar: (formData) => request.post('/users/avatar', formData, { headers: { 'Content-Type': 'multipart/form-data' } }),
  getProfile: () => request.get('/users/profile'),
  getProfileById: (id) => request.get('/users/profile/' + id),
  updateProfile: (data) => request.put('/users/profile', data),
  getWallet: () => request.get('/wallet'),
  getBills: (page, size) => request.get('/wallet/bills', { params: { page, size } }),
  recharge: (amount) => request.post('/payment/recharge', null, { params: { amount } }),
  withdraw: (amount) => request.post('/payment/withdraw', null, { params: { amount } }),
  follow: (userId) => request.post('/social/follow/' + userId),
  unfollow: (userId) => request.delete('/social/follow/' + userId),
  getFollowers: (userId) => request.get('/social/followers/' + userId),
  getFollowing: (userId) => request.get('/social/following/' + userId),
  isFollowing: (userId) => request.get('/social/is-following/' + userId),
  getNotifications: (page, size) => request.get('/notifications', { params: { page, size } }),
  getUnreadNotifications: () => request.get('/notifications/unread'),
  getUnreadCount: () => request.get('/notifications/unread-count'),
  markAsRead: (id) => request.put('/notifications/' + id + '/read'),
  markAllRead: () => request.put('/notifications/read-all'),
  getBadges() {
    return request.get('/users/badges')
  },
  getPoints() {
    return request.get('/points')
  },
  exchangePoints(type) {
    return request.post('/points/exchange', { type })
  },
  getPointsHistory() {
    return request.get('/points/history')
  },
}
