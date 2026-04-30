import request from './request'
export const analyticsApi = {
  getStatistics: (surveyId) => request.get('/analytics/surveys/' + surveyId),
  getFindings: (surveyId) => request.get('/analytics/surveys/' + surveyId + '/findings'),
  getSentiment: (surveyId, questionId) => request.get('/analytics/surveys/' + surveyId + '/sentiment/' + questionId),
  exportCsv: (surveyId) => '/api/v1/analytics/surveys/' + surveyId + '/export/csv',
}
