import React, { useEffect, useState } from 'react';
import { getClaimsPaged, markUnderReview, decideClaim, assignClaim } from '../services/claimService';
import { getEmployees } from '../services/employeeService';
import { downloadDocument } from '../services/documentService';
import { useAuth } from '../context/AuthContext';
import StatusBadge from '../components/StatusBadge';
import Pagination from '../components/Pagination';

const PAGE_SIZE = 10;
const STATUS_OPTIONS = ['ALL', 'PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED'];

export default function Claims() {
  const { user } = useAuth();
  const [pageData, setPageData] = useState({ content: [], page: 0, totalPages: 0, totalElements: 0 });
  const [agents, setAgents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [status, setStatus] = useState('ALL');
  const [searchInput, setSearchInput] = useState('');
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [expandedClaimId, setExpandedClaimId] = useState(null);
  const [decisionModal, setDecisionModal] = useState(null); // { claim, status }
  const [remarks, setRemarks] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (user.role === 'ADMIN') {
      getEmployees().then(({ data }) => setAgents(data.filter((e) => e.enabled))).catch(() => {});
    }
  }, []);

  useEffect(() => {
    load();
  }, [page, search, status]);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const { data } = await getClaimsPaged({
        page,
        size: PAGE_SIZE,
        search: search || undefined,
        status: status === 'ALL' ? undefined : status,
      });
      setPageData(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load claims');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0);
    setSearch(searchInput);
  };

  const handleStatusChange = (s) => {
    setStatus(s);
    setPage(0);
  };

  const handleReview = async (id) => {
    try {
      await markUnderReview(id);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update claim');
    }
  };

  const handleAssign = async (claimId, agentId) => {
    if (!agentId) return;
    try {
      await assignClaim(claimId, Number(agentId));
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to assign claim');
    }
  };

  const openDecision = (claim, status) => {
    setDecisionModal({ claim, status });
    setRemarks('');
  };

  const submitDecision = async () => {
    setSaving(true);
    try {
      await decideClaim(decisionModal.claim.id, { status: decisionModal.status, remarks });
      setDecisionModal(null);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to record decision');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6">
      <h2 className="text-xl font-bold text-gray-800">Claim Management</h2>

      <div className="flex flex-wrap items-center gap-3">
        <form onSubmit={handleSearch} className="flex gap-2">
          <input
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            placeholder="Search by policy #, customer, reason"
            className="input-field w-72"
          />
          <button type="submit" className="btn-secondary">Search</button>
        </form>
        <div className="flex gap-2 flex-wrap">
          {STATUS_OPTIONS.map((s) => (
            <button
              key={s}
              onClick={() => handleStatusChange(s)}
              className={`text-xs font-medium px-3 py-1.5 rounded-full transition-colors ${
                status === s ? 'bg-brand-500 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              {s}
            </button>
          ))}
        </div>
      </div>

      {error && <div className="text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">{error}</div>}

      <div className="card overflow-x-auto">
        {loading ? (
          <p className="text-gray-500">Loading claims...</p>
        ) : pageData.content.length === 0 ? (
          <p className="text-gray-500">No claims found.</p>
        ) : (
          <>
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-gray-500 border-b">
                  <th className="py-2 pr-4">Policy #</th>
                  <th className="py-2 pr-4">Customer</th>
                  <th className="py-2 pr-4">Amount</th>
                  <th className="py-2 pr-4">Reason</th>
                  <th className="py-2 pr-4">Submitted</th>
                  <th className="py-2 pr-4">Status</th>
                  <th className="py-2 pr-4">Documents</th>
                  {user.role === 'ADMIN' && <th className="py-2 pr-4">Assigned To</th>}
                  <th className="py-2 pr-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {pageData.content.map((c) => (
                  <React.Fragment key={c.id}>
                    <tr className="border-b last:border-0 hover:bg-gray-50">
                      <td className="py-3 pr-4 font-medium text-gray-800">{c.policyNumber}</td>
                      <td className="py-3 pr-4 text-gray-600">{c.customerName}</td>
                      <td className="py-3 pr-4 text-gray-600">₹{Number(c.claimAmount).toLocaleString()}</td>
                      <td className="py-3 pr-4 text-gray-600 max-w-xs truncate" title={c.reason}>{c.reason}</td>
                      <td className="py-3 pr-4 text-gray-500">{new Date(c.submissionDate).toLocaleDateString()}</td>
                      <td className="py-3 pr-4"><StatusBadge status={c.status} /></td>
                      <td className="py-3 pr-4">
                        {c.documents?.length ? (
                          <button
                            onClick={() => setExpandedClaimId(expandedClaimId === c.id ? null : c.id)}
                            className="text-brand-600 hover:underline text-xs whitespace-nowrap"
                          >
                            📎 {c.documents.length} file(s) {expandedClaimId === c.id ? '▲' : '▼'}
                          </button>
                        ) : (
                          <span className="text-xs text-gray-400">None</span>
                        )}
                      </td>
                      {user.role === 'ADMIN' && (
                        <td className="py-3 pr-4">
                          <select
                            value={c.assignedAgentId || ''}
                            onChange={(e) => handleAssign(c.id, e.target.value)}
                            className="text-xs border border-gray-200 rounded-lg px-2 py-1"
                          >
                            <option value="">Unassigned</option>
                            {agents.map((a) => (
                              <option key={a.id} value={a.id}>{a.name}</option>
                            ))}
                          </select>
                        </td>
                      )}
                      <td className="py-3 pr-4 text-right space-x-2 whitespace-nowrap">
                        {c.status === 'PENDING' && (
                          <button onClick={() => handleReview(c.id)} className="text-brand-600 hover:underline">Verify</button>
                        )}
                        {(c.status === 'PENDING' || c.status === 'UNDER_REVIEW') && (
                          <>
                            <button onClick={() => openDecision(c, 'APPROVED')} className="text-green-600 hover:underline">Approve</button>
                            <button onClick={() => openDecision(c, 'REJECTED')} className="text-red-600 hover:underline">Reject</button>
                          </>
                        )}
                      </td>
                    </tr>
                    {expandedClaimId === c.id && c.documents?.length > 0 && (
                      <tr className="bg-gray-50">
                        <td colSpan={9} className="px-4 py-3">
                          <p className="text-xs text-gray-500 mb-2 font-medium">Supporting documents for verification:</p>
                          <div className="flex flex-wrap gap-2">
                            {c.documents.map((d) => (
                              <button
                                key={d.id}
                                onClick={() => downloadDocument(d.id, d.fileName)}
                                className="text-xs bg-white border border-gray-200 rounded-lg px-3 py-1.5 hover:bg-gray-100"
                              >
                                📎 {d.fileName} <span className="text-gray-400">({d.documentType})</span>
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
            <Pagination page={pageData.page} totalPages={pageData.totalPages} totalElements={pageData.totalElements} onPageChange={setPage} />
          </>
        )}
      </div>

      {decisionModal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 px-4">
          <div className="bg-white rounded-2xl shadow-xl p-6 w-full max-w-md">
            <h3 className="text-lg font-bold text-gray-800 mb-1">
              {decisionModal.status === 'APPROVED' ? 'Approve Claim' : 'Reject Claim'}
            </h3>
            <p className="text-sm text-gray-500 mb-4">Policy {decisionModal.claim.policyNumber} · {decisionModal.claim.customerName}</p>

            {decisionModal.claim.documents?.length > 0 && (
              <div className="mb-4 bg-gray-50 border border-gray-100 rounded-lg p-3">
                <p className="text-xs text-gray-500 mb-2 font-medium">Attached supporting documents:</p>
                <div className="flex flex-wrap gap-2">
                  {decisionModal.claim.documents.map((d) => (
                    <button
                      key={d.id}
                      onClick={() => downloadDocument(d.id, d.fileName)}
                      className="text-xs bg-white border border-gray-200 rounded-lg px-3 py-1.5 hover:bg-gray-100"
                    >
                      📎 {d.fileName}
                    </button>
                  ))}
                </div>
              </div>
            )}
            {(!decisionModal.claim.documents || decisionModal.claim.documents.length === 0) && (
              <p className="text-xs text-amber-600 mb-4">⚠️ No supporting documents attached to this claim.</p>
            )}

            <label className="block text-sm font-medium text-gray-700 mb-1">Remarks (optional)</label>
            <textarea value={remarks} onChange={(e) => setRemarks(e.target.value)} rows={3} className="input-field" />
            <div className="flex justify-end gap-3 pt-4">
              <button onClick={() => setDecisionModal(null)} className="btn-secondary">Cancel</button>
              <button onClick={submitDecision} disabled={saving} className={decisionModal.status === 'APPROVED' ? 'btn-primary' : 'btn-danger'}>
                {saving ? 'Saving...' : `Confirm ${decisionModal.status === 'APPROVED' ? 'Approval' : 'Rejection'}`}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
