import request from './request'
export const authApi = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data),
  sendSms: (phone) => request.post('/auth/send-sms?phone=' + phone),
  refreshToken: (token) => request.post('/auth/refresh-token?refreshToken=' + token),
}
