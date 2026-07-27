import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import PasswordInput from '../components/PasswordInput';
import {
  getEmployees,
  createEmployee,
  updateEmployee,
  setEmployeeEnabled,
  deleteEmployee,
} from '../services/employeeService';

const EMPTY_FORM = { name: '', email: '', password: '', role: 'AGENT' };

export default function Employees() {
  const { user: currentUser } = useAuth();
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    load();
  }, []);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const { data } = await getEmployees();
      setEmployees(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load employees');
    } finally {
      setLoading(false);
    }
  };

  const openCreate = () => {
    setEditingId(null);
    setForm(EMPTY_FORM);
    setShowForm(true);
  };

  const openEdit = (emp) => {
    setEditingId(emp.id);
    setForm({ name: emp.name, email: emp.email, password: '', role: emp.role });
    setShowForm(true);
  };

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      const payload = { ...form };
      if (editingId && !payload.password) delete payload.password;
      if (editingId) {
        await updateEmployee(editingId, payload);
      } else {
        await createEmployee(payload);
      }
      setShowForm(false);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save employee');
    } finally {
      setSaving(false);
    }
  };

  const handleToggleEnabled = async (emp) => {
    try {
      await setEmployeeEnabled(emp.id, !emp.enabled);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update employee status');
    }
  };

  const handleDelete = async (emp) => {
    if (!window.confirm(`Remove ${emp.name} (${emp.role})? This cannot be undone.`)) return;
    try {
      await deleteEmployee(emp.id);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete employee');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-gray-800">Employee Management</h2>
          <p className="text-sm text-gray-500">Manage Administrator and Agent accounts</p>
        </div>
        <button onClick={openCreate} className="btn-primary">+ Add Employee</button>
      </div>

      {error && <div className="text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">{error}</div>}

      <div className="card overflow-x-auto">
        {loading ? (
          <p className="text-gray-500">Loading employees...</p>
        ) : employees.length === 0 ? (
          <p className="text-gray-500">No employees found.</p>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-gray-500 border-b">
                <th className="py-2 pr-4">Name</th>
                <th className="py-2 pr-4">Email</th>
                <th className="py-2 pr-4">Role</th>
                <th className="py-2 pr-4">Status</th>
                <th className="py-2 pr-4">Joined</th>
                <th className="py-2 pr-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {employees.map((emp) => (
                <tr key={emp.id} className="border-b last:border-0 hover:bg-gray-50">
                  <td className="py-3 pr-4 font-medium text-gray-800">{emp.name}</td>
                  <td className="py-3 pr-4 text-gray-600">{emp.email}</td>
                  <td className="py-3 pr-4">
                    <span className={`badge ${emp.role === 'ADMIN' ? 'bg-purple-100 text-purple-700' : 'bg-blue-100 text-blue-700'}`}>
                      {emp.role}
                    </span>
                  </td>
                  <td className="py-3 pr-4">
                    <span className={`badge ${emp.enabled ? 'bg-green-100 text-green-700' : 'bg-gray-200 text-gray-600'}`}>
                      {emp.enabled ? 'Active' : 'Disabled'}
                    </span>
                  </td>
                  <td className="py-3 pr-4 text-gray-500">{emp.createdAt ? new Date(emp.createdAt).toLocaleDateString() : '—'}</td>
                  <td className="py-3 pr-4 text-right space-x-2 whitespace-nowrap">
                    <button onClick={() => openEdit(emp)} className="text-brand-600 hover:underline">Edit</button>
                    {emp.email !== currentUser.email && (
                      <>
                        <button onClick={() => handleToggleEnabled(emp)} className="text-amber-600 hover:underline">
                          {emp.enabled ? 'Disable' : 'Enable'}
                        </button>
                        <button onClick={() => handleDelete(emp)} className="text-red-600 hover:underline">Delete</button>
                      </>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {showForm && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 px-4">
          <div className="bg-white rounded-2xl shadow-xl p-6 w-full max-w-md">
            <h3 className="text-lg font-bold text-gray-800 mb-4">{editingId ? 'Edit Employee' : 'Add Employee'}</h3>
            <form onSubmit={handleSubmit} className="space-y-4" autoComplete="off">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Full name</label>
                <input name="name" required autoComplete="off" value={form.name} onChange={handleChange} className="input-field" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                <input type="email" name="email" required autoComplete="off" value={form.email} onChange={handleChange} className="input-field" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Password {editingId && <span className="text-gray-400 font-normal">(leave blank to keep current)</span>}
                </label>
                <PasswordInput name="password" minLength={6} autoComplete="new-password" value={form.password} onChange={handleChange} className="input-field" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Role</label>
                <select name="role" required value={form.role} onChange={handleChange} className="input-field">
                  <option value="AGENT">Agent</option>
                  <option value="ADMIN">Administrator</option>
                </select>
              </div>
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
