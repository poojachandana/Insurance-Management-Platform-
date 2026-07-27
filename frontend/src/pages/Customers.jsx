import React, { useEffect, useState } from 'react';
import { getCustomersPaged, createCustomer, updateCustomer, deleteCustomer, assignCustomerAgent } from '../services/customerService';
import { getEmployees } from '../services/employeeService';
import { useAuth } from '../context/AuthContext';
import Pagination from '../components/Pagination';

const EMPTY_FORM = { name: '', email: '', phone: '', address: '', dob: '' };
const PAGE_SIZE = 10;

export default function Customers() {
  const { user } = useAuth();
  const [pageData, setPageData] = useState({ content: [], page: 0, totalPages: 0, totalElements: 0 });
  const [agents, setAgents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    load();
    if (user.role === 'ADMIN') {
      getEmployees().then(({ data }) => setAgents(data.filter((e) => e.enabled))).catch(() => {});
    }
  }, [page, search]);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const { data } = await getCustomersPaged({ page, size: PAGE_SIZE, search: search || undefined });
      setPageData(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load customers');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0);
    setSearch(searchInput);
  };

  const openCreate = () => {
    setEditingId(null);
    setForm(EMPTY_FORM);
    setShowForm(true);
  };

  const openEdit = (customer) => {
    setEditingId(customer.id);
    setForm({
      name: customer.name,
      email: customer.email,
      phone: customer.phone || '',
      address: customer.address || '',
      dob: customer.dob || '',
    });
    setShowForm(true);
  };

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      const payload = { ...form, dob: form.dob || null };
      if (editingId) {
        await updateCustomer(editingId, payload);
      } else {
        await createCustomer(payload);
      }
      setShowForm(false);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save customer');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this customer? This cannot be undone.')) return;
    try {
      await deleteCustomer(id);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete customer');
    }
  };

  const handleAssign = async (customerId, agentId) => {
    try {
      await assignCustomerAgent(customerId, agentId ? Number(agentId) : null);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to assign customer');
    }
  };

  return (
      <div className="space-y-6">
        <div className="flex items-center justify-between flex-wrap gap-3">
          <div>
            <h2 className="text-xl font-bold text-gray-800">Customer Management</h2>
            {user.role === 'ADMIN' && (
                <p className="text-sm text-gray-500 mt-0.5">
                  Customers who self-register show as <span className="font-medium text-amber-600">Unassigned</span> until you assign them to an Agent.
                </p>
            )}
          </div>
          <div className="flex gap-3">
            <form onSubmit={handleSearch} className="flex gap-2">
              <input
                  value={searchInput}
                  onChange={(e) => setSearchInput(e.target.value)}
                  placeholder="Search by name, email, phone"
                  className="input-field w-64"
              />
              <button type="submit" className="btn-secondary">Search</button>
            </form>
            <button onClick={openCreate} className="btn-primary">+ Register Customer</button>
          </div>
        </div>

        {error && <div className="text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">{error}</div>}

        <div className="card overflow-x-auto">
          {loading ? (
              <p className="text-gray-500">Loading customers...</p>
          ) : pageData.content.length === 0 ? (
              <p className="text-gray-500">No customers found.</p>
          ) : (
              <>
                <table className="w-full text-sm">
                  <thead>
                  <tr className="text-left text-gray-500 border-b">
                    <th className="py-2 pr-4">Name</th>
                    <th className="py-2 pr-4">Email</th>
                    <th className="py-2 pr-4">Phone</th>
                    <th className="py-2 pr-4">Address</th>
                    <th className="py-2 pr-4">Joined</th>
                    {user.role === 'ADMIN' && <th className="py-2 pr-4">Assigned Agent</th>}
                    <th className="py-2 pr-4 text-right">Actions</th>
                  </tr>
                  </thead>
                  <tbody>
                  {pageData.content.map((c) => (
                      <tr key={c.id} className="border-b last:border-0 hover:bg-gray-50">
                        <td className="py-3 pr-4 font-medium text-gray-800">{c.name}</td>
                        <td className="py-3 pr-4 text-gray-600">{c.email}</td>
                        <td className="py-3 pr-4 text-gray-600">{c.phone || '—'}</td>
                        <td className="py-3 pr-4 text-gray-600">{c.address || '—'}</td>
                        <td className="py-3 pr-4 text-gray-500">{c.createdAt ? new Date(c.createdAt).toLocaleDateString() : '—'}</td>
                        {user.role === 'ADMIN' && (
                            <td className="py-3 pr-4">
                              <select
                                  value={c.assignedAgentId || ''}
                                  onChange={(e) => handleAssign(c.id, e.target.value)}
                                  className={`text-xs border rounded-lg px-2 py-1 ${
                                      c.assignedAgentId ? 'border-gray-200' : 'border-amber-300 bg-amber-50 text-amber-700'
                                  }`}
                              >
                                <option value="">Unassigned</option>
                                {agents.map((a) => (
                                    <option key={a.id} value={a.id}>{a.name} ({a.role})</option>
                                ))}
                              </select>
                            </td>
                        )}
                        <td className="py-3 pr-4 text-right space-x-2">
                          <button onClick={() => openEdit(c)} className="text-brand-600 hover:underline">Edit</button>
                          {user.role === 'ADMIN' && (
                              <button onClick={() => handleDelete(c.id)} className="text-red-600 hover:underline">Delete</button>
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
                <h3 className="text-lg font-bold text-gray-800 mb-4">{editingId ? 'Edit Customer' : 'Register Customer'}</h3><form onSubmit={handleSubmit} className="space-y-4" autoComplete="off">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Full name</label>
                  <input name="name" required autoComplete="off" value={form.name} onChange={handleChange} className="input-field" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                  <input type="email" name="email" required autoComplete="off" value={form.email} onChange={handleChange} className="input-field" />
                </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Phone</label>
                      <input name="phone" value={form.phone} onChange={handleChange} className="input-field" />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Date of birth</label>
                      <input type="date" name="dob" value={form.dob} onChange={handleChange} className="input-field" />
                    </div>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Address</label>
                    <input name="address" value={form.address} onChange={handleChange} className="input-field" />
                  </div>
                  {!editingId && (
                      <p className="text-xs text-gray-400 -mt-2">
                        This customer will be assigned to you. If they self-register instead, they'll start unassigned
                        and an Admin can route them to an Agent later.
                      </p>
                  )}
                  <div className="flex justify-end gap-3 pt-2">
                    <button type="button" onClick={() => setShowForm(false)} className="btn-secondary">Cancel</button>
                    <button type="submit" disabled={saving} className="btn-primary">{saving ? 'Saving...' : 'Save'}</button>
                  </div>
                </form>
              </div>
            </div>
        )}
      </div>
  );
}