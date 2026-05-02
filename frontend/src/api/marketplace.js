import request from './request'
export const marketplaceApi = {
  listSurveys: (params) => request.get('/marketplace/surveys', { params }),
  claim: (id) => request.post('/marketplace/surveys/' + id + '/claim'),
  getRecommended: () => request.get('/marketplace/recommended'),
  getLeaderboard: (period) => request.get('/leaderboard', { params: { period } }),
  checkIn: () => request.post('/checkin'),
  getCheckinStatus: () => request.get('/checkin/status'),
}
