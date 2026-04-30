import request from './request'
export const adminApi = {
  getUsers: (params) => request.get('/admin/users', { params }),
  banUser: (id, reason) => request.put('/admin/users/' + id + '/ban?reason=' + reason),
  unbanUser: (id) => request.put('/admin/users/' + id + '/unban'),
  getSurveys: (params) => request.get('/admin/surveys', { params }),
  approveSurvey: (id) => request.put('/admin/surveys/' + id + '/approve'),
  rejectSurvey: (id, reason) => request.put('/admin/surveys/' + id + '/reject?reason=' + reason),
  removeSurvey: (id) => request.put('/admin/surveys/' + id + '/remove'),
  getTransactions: (params) => request.get('/admin/finance/transactions', { params }),
  getDashboard: () => request.get('/admin/dashboard'),
}
