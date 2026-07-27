import React, { useEffect, useState } from 'react';
import { getMyClaims, submitClaim } from '../services/claimService';
import { getMyPolicies } from '../services/policyService';
import { getMyProfile } from '../services/customerService';
import { getDocumentsByCustomer, uploadDocument, downloadDocument } from '../services/documentService';
import StatusBadge from '../components/StatusBadge';

const EMPTY_FORM = { policyId: '', claimAmount: '', reason: '' };

export default function MyClaims() {
  const [claims, setClaims] = useState([]);
  const [policies, setPolicies] = useState([]);
  const [customerId, setCustomerId] = useState(null);
  const [myDocuments, setMyDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [selectedDocIds, setSelectedDocIds] = useState([]);
  const [uploadingDoc, setUploadingDoc] = useState(false);
  const [saving, setSaving] = useState(false);
  const [expandedClaimId, setExpandedClaimId] = useState(null);

  useEffect(() => {
    load();
    getMyPolicies().then(({ data }) => setPolicies(data.filter((p) => p.status === 'ACTIVE'))).catch(() => {});
    getMyProfile().then(({ data }) => {
      setCustomerId(data.id);
      getDocumentsByCustomer(data.id).then(({ data: docs }) => setMyDocuments(docs)).catch(() => {});
    }).catch(() => {});
  }, []);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const { data } = await getMyClaims();
      setClaims(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load your claims');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const toggleDoc = (id) => {
    setSelectedDocIds((prev) => (prev.includes(id) ? prev.filter((d) => d !== id) : [...prev, id]));
  };

  const handleQuickUpload = async (e) => {
    const file = e.target.files[0];
    if (!file || !customerId) return;
    setUploadingDoc(true);
    setError('');
    try {
      const { data: newDoc } = await uploadDocument(customerId, null, 'CLAIM', file);
      setMyDocuments((prev) => [newDoc, ...prev]);
      setSelectedDocIds((prev) => [...prev, newDoc.id]);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload document');
    } finally {
      setUploadingDoc(false);
      e.target.value = '';
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      await submitClaim({
        ...form,
        policyId: Number(form.policyId),
        claimAmount: Number(form.claimAmount),
        documentIds: selectedDocIds,
      });
      setShowForm(false);
      setForm(EMPTY_FORM);
      setSelectedDocIds([]);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit claim');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-bold text-gray-800">My Claims</h2>
        <button onClick={() => setShowForm(true)} className="btn-primary" disabled={policies.length === 0}>+ Submit Claim</button>
      </div>

      {error && <div className="text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">{error}</div>}
      {policies.length === 0 && !loading && (
        <div className="text-sm text-amber-700 bg-amber-50 border border-amber-100 rounded-lg px-3 py-2">
          You need at least one active policy before you can submit a claim.
        </div>
      )}

      <div className="card overflow-x-auto">
        {loading ? (
          <p className="text-gray-500">Loading...</p>
        ) : claims.length === 0 ? (
          <p className="text-gray-500">You haven't submitted any claims yet.</p>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-gray-500 border-b">
                <th className="py-2 pr-4">Policy #</th>
                <th className="py-2 pr-4">Amount</th>
                <th className="py-2 pr-4">Reason</th>
                <th className="py-2 pr-4">Submitted</th>
                <th className="py-2 pr-4">Status</th>
                <th className="py-2 pr-4">Documents</th>
                <th className="py-2 pr-4">Remarks</th>
              </tr>
            </thead>
            <tbody>
              {claims.map((c) => (
                <React.Fragment key={c.id}>
                  <tr className="border-b last:border-0">
                    <td className="py-3 pr-4 font-medium text-gray-800">{c.policyNumber}</td>
                    <td className="py-3 pr-4 text-gray-600">₹{Number(c.claimAmount).toLocaleString()}</td>
                    <td className="py-3 pr-4 text-gray-600">{c.reason}</td>
                    <td className="py-3 pr-4 text-gray-500">{new Date(c.submissionDate).toLocaleDateString()}</td>
                    <td className="py-3 pr-4"><StatusBadge status={c.status} /></td>
                    <td className="py-3 pr-4">
                      {c.documents?.length ? (
                        <button
                          onClick={() => setExpandedClaimId(expandedClaimId === c.id ? null : c.id)}
                          className="text-brand-600 hover:underline text-xs"
                        >
                          {c.documents.length} file(s) {expandedClaimId === c.id ? '▲' : '▼'}
                        </button>
                      ) : (
                        <span className="text-xs text-gray-400">None</span>
                      )}
                    </td>
                    <td className="py-3 pr-4 text-gray-500">{c.rejectionRemarks || '—'}</td>
                  </tr>
                  {expandedClaimId === c.id && c.documents?.length > 0 && (
                    <tr className="bg-gray-50">
                      <td colSpan={7} className="px-4 py-3">
                        <div className="flex flex-wrap gap-2">
                          {c.documents.map((d) => (
                            <button
                              key={d.id}
                              onClick={() => downloadDocument(d.id, d.fileName)}
                              className="text-xs bg-white border border-gray-200 rounded-lg px-3 py-1.5 hover:bg-gray-100"
                            >
                              📎 {d.fileName}
                            </button>
                          ))}
                        </div>
                      </td>
                    </tr>
                  )}
                </React.Fragment>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {showForm && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 px-4 py-8 overflow-y-auto">
          <div className="bg-white rounded-2xl shadow-xl p-6 w-full max-w-md my-auto">
            <h3 className="text-lg font-bold text-gray-800 mb-4">Submit Insurance Claim</h3>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Policy</label>
                <select name="policyId" required value={form.policyId} onChange={handleChange} className="input-field">
                  <option value="">Select a policy</option>
                  {policies.map((p) => (
                    <option key={p.id} value={p.id}>{p.policyNumber} · {p.categoryName || p.policyType}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Claim amount</label>
                <input type="number" min="0" step="0.01" name="claimAmount" required value={form.claimAmount} onChange={handleChange} className="input-field" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Reason</label>
                <textarea name="reason" required rows={3} value={form.reason} onChange={handleChange} className="input-field" />
              </div>

              <div className="border-t pt-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">Supporting documents</label>

                {myDocuments.length > 0 ? (
                  <div className="max-h-36 overflow-y-auto space-y-1.5 mb-3 border border-gray-100 rounded-lg p-2">
                    {myDocuments.map((doc) => (
                      <label key={doc.id} className="flex items-center gap-2 text-sm cursor-pointer">
                        <input
                          type="checkbox"
                          checked={selectedDocIds.includes(doc.id)}
                          onChange={() => toggleDoc(doc.id)}
                        />
                        <span className="text-gray-700">{doc.fileName}</span>
                        <span className="text-xs text-gray-400">({doc.documentType})</span>
                      </label>
                    ))}
                  </div>
                ) : (
                  <p className="text-xs text-gray-400 mb-3">No previously uploaded documents yet — upload one below.</p>
                )}

                <div className="flex items-center gap-2">
                  <input type="file" onChange={handleQuickUpload} disabled={uploadingDoc} className="text-xs" />
                  {uploadingDoc && <span className="text-xs text-gray-400">Uploading...</span>}
                </div>
                <p className="text-xs text-gray-400 mt-1">
                  {selectedDocIds.length > 0 ? `${selectedDocIds.length} document(s) will be attached` : 'No documents selected'}
                </p>
              </div>

              <div className="flex justify-end gap-3 pt-2">
                <button type="button" onClick={() => setShowForm(false)} className="btn-secondary">Cancel</button>
                <button type="submit" disabled={saving} className="btn-primary">{saving ? 'Submitting...' : 'Submit Claim'}</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
