import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import PasswordInput from '../components/PasswordInput';

const ROLE_TABS = [
  { key: 'CUSTOMER', label: 'Customer', icon: '👤' },
  { key: 'AGENT', label: 'Agent', icon: '🧑‍💼' },
  { key: 'ADMIN', label: 'Admin', icon: '🛡️' },
];

const ROLE_COPY = {
  CUSTOMER: {
    subtitle: 'Sign in to manage your policies, claims and payments',
    hint: null,
  },
  AGENT: {
    subtitle: 'Sign in to your Agent workspace',
    hint: 'Agent accounts are created by an Administrator — contact yours if you need one.',
  },
  ADMIN: {
    subtitle: 'Sign in to the Administrator console',
    hint: 'Demo admin login: admin@insurance.com / Admin@123',
  },
};

export default function Login() {
  const { login, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [role, setRole] = useState('CUSTOMER');
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleRoleChange = (key) => {
    setRole(key);
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const user = await login(form.email, form.password);

      if (user.role !== role) {
        // Credentials were valid, but for a different portal than the one selected.
        logout();
        const tabLabel = ROLE_TABS.find((t) => t.key === role)?.label;
        setError(`This account isn't a ${tabLabel} account. Please choose the correct login tab above.`);
        return;
      }

      const dest = location.state?.from?.pathname || (user.role === 'CUSTOMER' ? '/my-policies' : '/dashboard');
      navigate(dest, { replace: true });
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid email or password');
    } finally {
      setSubmitting(false);
    }
  };

  const copy = ROLE_COPY[role];

  return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-brand-900 via-brand-700 to-brand-500 px-4">
        <div className="w-full max-w-md bg-white rounded-2xl shadow-xl p-8">
          <div className="text-center mb-6">
            <div className="w-12 h-12 rounded-xl bg-brand-500 text-white flex items-center justify-center text-2xl mx-auto mb-3">🛡️</div>
            <h1 className="text-xl font-bold text-gray-800">Insurance Management Platform</h1>
            <p className="text-sm text-gray-500 mt-1">{copy.subtitle}</p>
          </div>

          <div className="grid grid-cols-3 gap-2 mb-6 bg-gray-100 rounded-xl p-1">
            {ROLE_TABS.map((tab) => (
                <button
                    key={tab.key}
                    type="button"
                    onClick={() => handleRoleChange(tab.key)}
                    className={`flex flex-col items-center gap-1 py-2 rounded-lg text-sm font-medium transition-colors ${
                        role === tab.key ? 'bg-white shadow text-brand-600' : 'text-gray-500 hover:text-gray-700'
                    }`}
                >
                  <span className="text-lg leading-none">{tab.icon}</span>
                  {tab.label}
                </button>
            ))}
          </div>

          {error && <div className="mb-4 text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">{error}</div>}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
              <input type="email" name="email" required value={form.email} onChange={handleChange}
                     className="input-field" placeholder="you@example.com" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
              <PasswordInput name="password" required value={form.password} onChange={handleChange}
                             className="input-field" placeholder="••••••••" />
            </div>
            <button type="submit" disabled={submitting} className="btn-primary w-full justify-center flex">
              {submitting ? 'Signing in...' : `Sign In as ${ROLE_TABS.find((t) => t.key === role)?.label}`}
            </button>
          </form>

          {role === 'CUSTOMER' && (
              <p className="text-sm text-gray-500 text-center mt-6">
                Don't have an account? <Link to="/register" className="text-brand-600 font-medium hover:underline">Register</Link>
              </p>
          )}

          {copy.hint && (
              <div className="mt-6 text-xs text-gray-400 border-t pt-4">
                <p>{copy.hint}</p>
              </div>
          )}
        </div>
      </div>
  );
}