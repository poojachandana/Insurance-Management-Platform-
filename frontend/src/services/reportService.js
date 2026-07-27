import api from './api';

export const getReportSummary = () => api.get('/reports/summary');

// Uses the authenticated axios instance since the PDF endpoint requires a Bearer token.
export const downloadMonthlyReportPdf = async () => {
  const response = await api.get('/reports/monthly-report/pdf', { responseType: 'blob' });
  const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', 'monthly-business-report.pdf');
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
};
