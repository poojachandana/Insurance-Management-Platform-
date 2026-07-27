import api from './api';

export const getClaims = () => api.get('/claims');
export const getClaimsPaged = (params) => api.get('/claims/paged', { params });
export const getPendingClaims = () => api.get('/claims/pending');
export const getMyClaims = () => api.get('/claims/me');
export const getAssignedToMeClaims = () => api.get('/claims/assigned/me');
export const getClaimsByPolicy = (policyId) => api.get(`/claims/policy/${policyId}`);
export const submitClaim = (payload) => api.post('/claims', payload);
export const attachDocumentsToClaim = (claimId, documentIds) => api.patch(`/claims/${claimId}/documents`, { documentIds });
export const assignClaim = (id, agentId) => api.patch(`/claims/${id}/assign`, { agentId });
export const markUnderReview = (id) => api.patch(`/claims/${id}/review`);
export const decideClaim = (id, payload) => api.patch(`/claims/${id}/decision`, payload);
