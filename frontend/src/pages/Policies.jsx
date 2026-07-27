import React, { useEffect, useState } from 'react';
import { getPoliciesPaged, createPolicy, renewPolicy, cancelPolicy } from '../services/policyService';
import { getCustomers } from '../services/customerService';
import { getActiveCategories } from '../services/policyCategoryService';
import StatusBadge from '../components/StatusBadge';
import Pagination from '../components/Pagination';

const EMPTY_FORM = { customerId: '', categoryId: '', premiumAmount: '', startDate: '', endDate: '' };
const PAGE_SIZE = 10;
const STATUS_OPTIONS = ['ALL', 'ACTIVE', 'EXPIRED', 'CANCELLED', 'RENEWED'];

export default function Policies() {
  const [pageData, setPageData] = useState({ content: [], page: 0, totalPages: 0, totalElements: 0 });
  const [customers, setCustomers] = useState([]);
  const [categories, setCategories] = useState([]);
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
    getCustomers().then(({ data }) => setCustomers(data)).catch(() => {});
    getActiveCategories().then(({ data }) => setCategories(data)).catch(() => {});
  }, []);

  useEffect(() => {
    load();
  }, [page, search, status]);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const { data } = await getPoliciesPaged({
        page,
        size: PAGE_SIZE,
        search: search || undefined,
        status: status === 'ALL' ? undefined : status,
      });
      setPageData(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load policies');
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
      await createPolicy({
        ...form,
        customerId: Number(form.customerId),
        categoryId: Number(form.categoryId),
        premiumAmount: Number(form.premiumAmount),
      });
      setShowForm(false);
      setForm(EMPTY_FORM);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create policy');
    } finally {
      setSaving(false);
    }
  };

  const handleRenew = async (id) => {
    try {
      await renewPolicy(id, 12);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to renew policy');
    }
  };

  const handleCancel = async (id) => {
    if (!window.confirm('Cancel this policy?')) return;
    try {
      await cancelPolicy(id);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to cancel policy');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <h2 className="text-xl font-bold text-gray-800">Policy Management</h2>
        <button onClick={() => setShowForm(true)} className="btn-primary">+ Create Policy</button>
      </div>

      <div className="flex flex-wrap items-center gap-3">
        <form onSubmit={handleSearch} className="flex gap-2">
          <input
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            placeholder="Search by policy #, customer, category"
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
          <p className="text-gray-500">Loading policies...</p>
        ) : pageData.content.length === 0 ? (
          <p className="text-gray-500">No policies found.</p>
        ) : (
          <>
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-gray-500 border-b">
                  <th className="py-2 pr-4">Policy #</th>
                  <th className="py-2 pr-4">Customer</th>
                  <th className="py-2 pr-4">Category</th>
                  <th className="py-2 pr-4">Premium</th>
                  <th className="py-2 pr-4">Start</th>
                  <th className="py-2 pr-4">End</th>
                  <th className="py-2 pr-4">Status</th>
                  <th className="py-2 pr-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {pageData.content.map((p) => (
                  <tr key={p.id} className="border-b last:border-0 hover:bg-gray-50">
                    <td className="py-3 pr-4 font-medium text-gray-800">{p.policyNumber}</td>
                    <td className="py-3 pr-4 text-gray-600">{p.customerName}</td>
                    <td className="py-3 pr-4 text-gray-600">{p.categoryName || p.policyType}</td>
                    <td className="py-3 pr-4 text-gray-600">₹{Number(p.premiumAmount).toLocaleString()}</td>
                    <td className="py-3 pr-4 text-gray-500">{p.startDate}</td>
                    <td className="py-3 pr-4 text-gray-500">{p.endDate}</td>
                    <td className="py-3 pr-4"><StatusBadge status={p.status} /></td>
                    <td className="py-3 pr-4 text-right space-x-2">
                      <button onClick={() => handleRenew(p.id)} className="text-brand-600 hover:underline">Renew</button>
                      {p.status !== 'CANCELLED' && (
                        <button onClick={() => handleCancel(p.id)} className="text-red-600 hover:underline">Cancel</button>
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
          <div className="bg-white rounded-2xl shadow-xl p-6 w-full max-w-lg">
            <h3 className="text-lg font-bold text-gray-800 mb-4">Create Insurance Policy</h3>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Customer</label>
                <select name="customerId" required value={form.customerId} onChange={handleChange} className="input-field">
                  <option value="">Select a customer</option>
                  {customers.map((c) => (
                    <option key={c.id} value={c.id}>{c.name} ({c.email})</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Insurance category</label>
                <select name="categoryId" required value={form.categoryId} onChange={handleChange} className="input-field">
                  <option value="">Select a category</option>
                  {categories.map((c) => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
                {categories.length === 0 && (
                  <p className="text-xs text-amber-600 mt-1">No active categories found — add one under Insurance Categories first.</p>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Premium amount</label>
                <input type="number" min="0" step="0.01" name="premiumAmount" required value={form.premiumAmount} onChange={handleChange} className="input-field" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Start date</label>
                  <input type="date" name="startDate" required value={form.startDate} onChange={handleChange} className="input-field" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">End date</label>
                  <input type="date" name="endDate" required value={form.endDate} onChange={handleChange} className="input-field" />
                </div>
              </div>
              <div className="flex justify-end gap-3 pt-2">
                <button type="button" onClick={() => setShowForm(false)} className="btn-secondary">Cancel</button>
                <button type="submit" disabled={saving} className="btn-primary">{saving ? 'Saving...' : 'Create Policy'}</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
