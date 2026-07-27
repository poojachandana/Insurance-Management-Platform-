import React, { useEffect, useState } from 'react';
import { getPremiumsPaged, createDuePremium, payPremium } from '../services/premiumService';
import { getPolicies } from '../services/policyService';
import StatusBadge from '../components/StatusBadge';
import Pagination from '../components/Pagination';

const EMPTY_FORM = { policyId: '', dueDate: '', amount: '' };
const PAGE_SIZE = 10;
const STATUS_OPTIONS = ['ALL', 'DUE', 'PAID', 'OVERDUE'];

export default function Premiums() {
  const [pageData, setPageData] = useState({ content: [], page: 0, totalPages: 0, totalElements: 0 });
  const [policies, setPolicies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [status, setStatus] = useState('ALL');
  const [searchInput, setSearchInput] = useState('');
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    getPolicies().then(({ data }) => setPolicies(data)).catch(() => {});
  }, []);

  useEffect(() => {
    load();
  }, [page, search, status]);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const { data } = await getPremiumsPaged({
        page,
        size: PAGE_SIZE,
        search: search || undefined,
        status: status === 'ALL' ? undefined : status,
      });
      setPageData(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load premium payments');
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

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      await createDuePremium({ ...form, policyId: Number(form.policyId), amount: Number(form.amount) });
      setShowForm(false);
      setForm(EMPTY_FORM);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to schedule premium');
    } finally {
      setSaving(false);
    }
  };

  const handlePay = async (id) => {
    try {
      await payPremium(id);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to record payment');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <h2 className="text-xl font-bold text-gray-800">Premium Tracking</h2>
        <button onClick={() => setShowForm(true)} className="btn-primary">+ Schedule Premium</button>
      </div>

      <div className="flex flex-wrap items-center gap-3">
        <form onSubmit={handleSearch} className="flex gap-2">
          <input
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            placeholder="Search by policy #, customer"
            className="input-field w-72"
          />
          <button type="submit" className="btn-secondary">Search</button>
        </form>
        <div className="flex gap-2">
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
          <p className="text-gray-500">Loading...</p>
        ) : pageData.content.length === 0 ? (
          <p className="text-gray-500">No premium records found.</p>
        ) : (
          <>
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-gray-500 border-b">
                  <th className="py-2 pr-4">Policy #</th>
                  <th className="py-2 pr-4">Customer</th>
                  <th className="py-2 pr-4">Amount</th>
                  <th className="py-2 pr-4">Due Date</th>
                  <th className="py-2 pr-4">Paid On</th>
                  <th className="py-2 pr-4">Status</th>
                  <th className="py-2 pr-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {pageData.content.map((p) => (
                  <tr key={p.id} className="border-b last:border-0 hover:bg-gray-50">
                    <td className="py-3 pr-4 font-medium text-gray-800">{p.policyNumber}</td>
                    <td className="py-3 pr-4 text-gray-600">{p.customerName}</td>
                    <td className="py-3 pr-4 text-gray-600">₹{Number(p.amount).toLocaleString()}</td>
                    <td className="py-3 pr-4 text-gray-500">{p.dueDate}</td>
                    <td className="py-3 pr-4 text-gray-500">{p.paymentDate || '—'}</td>
                    <td className="py-3 pr-4"><StatusBadge status={p.paymentStatus} /></td>
                    <td className="py-3 pr-4 text-right">
                      {p.paymentStatus !== 'PAID' && (
                        <button onClick={() => handlePay(p.id)} className="text-brand-600 hover:underline">Mark Paid</button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <Pagination page={pageData.page} totalPages={pageData.totalPages} totalElements={pageData.totalElements} onPageChange={setPage} />
          </>
        )}
      </div>

      {showForm && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 px-4">
          <div className="bg-white rounded-2xl shadow-xl p-6 w-full max-w-md">
            <h3 className="text-lg font-bold text-gray-800 mb-4">Schedule Premium Payment</h3>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Policy</label>
                <select name="policyId" required value={form.policyId} onChange={handleChange} className="input-field">
                  <option value="">Select a policy</option>
                  {policies.map((p) => (
                    <option key={p.id} value={p.id}>{p.policyNumber} · {p.customerName}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Amount</label>
                <input type="number" min="0" step="0.01" name="amount" required value={form.amount} onChange={handleChange} className="input-field" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Due date</label>
                <input type="date" name="dueDate" required value={form.dueDate} onChange={handleChange} className="input-field" />
              </div>
              <div className="flex justify-end gap-3 pt-2">
                <button type="button" onClick={() => setShowForm(false)} className="btn-secondary">Cancel</button>
                <button type="submit" disabled={saving} className="btn-primary">{saving ? 'Saving...' : 'Schedule'}</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
