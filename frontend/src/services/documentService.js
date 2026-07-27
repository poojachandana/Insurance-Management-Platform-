import api from './api';

export const getAllDocuments = () => api.get('/documents');
export const getDocumentsPaged = (params) => api.get('/documents/paged', { params });
export const getDocumentsByCustomer = (customerId) => api.get(`/documents/customer/${customerId}`);
export const getDocumentsByClaim = (claimId) => api.get(`/documents/claim/${claimId}`);
export const uploadDocument = (customerId, claimId, documentType, file) => {
  const formData = new FormData();
  formData.append('customerId', customerId);
  if (claimId) formData.append('claimId', claimId);
  formData.append('documentType', documentType);
  formData.append('file', file);
  return api.post('/documents', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};
export const deleteDocument = (id) => api.delete(`/documents/${id}`);

// Downloads use the authenticated axios instance (JWT header) rather than a raw
// <a href> link, since the API requires a Bearer token on /documents/{id}/download.
export const downloadDocument = async (id, fileName) => {
  const response = await api.get(`/documents/${id}/download`, { responseType: 'blob' });
  const url = window.URL.createObjectURL(new Blob([response.data]));
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', fileName || `document-${id}`);
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
};
