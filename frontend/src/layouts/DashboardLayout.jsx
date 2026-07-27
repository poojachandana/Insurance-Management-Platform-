import React from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import NotificationBell from '../components/NotificationBell';

const NAV_ITEMS = {
  ADMIN: [
    { to: '/dashboard', label: 'Reports Dashboard', icon: '📊' },
    { to: '/customers', label: 'Customers', icon: '👥' },
    { to: '/policies', label: 'Policies', icon: '📄' },
    { to: '/claims', label: 'Claims', icon: '🧾' },
    { to: '/premiums', label: 'Premiums', icon: '💳' },
    { to: '/documents', label: 'Documents', icon: '📁' },
    { to: '/policy-categories', label: 'Insurance Categories', icon: '🏷️' },
    { to: '/employees', label: 'Employees', icon: '🧑‍💼' },
    { to: '/audit-logs', label: 'Audit Logs', icon: '📜' },
    { to: '/settings', label: 'Settings', icon: '⚙️' },
  ],
  AGENT: [
    { to: '/dashboard', label: 'Reports Dashboard', icon: '📊' },
    { to: '/customers', label: 'Customers', icon: '👥' },
    { to: '/policies', label: 'Policies', icon: '📄' },
    { to: '/claims', label: 'Claims', icon: '🧾' },
    { to: '/premiums', label: 'Premiums', icon: '💳' },
    { to: '/documents', label: 'Documents', icon: '📁' },
  ],
  CUSTOMER: [
    { to: '/my-policies', label: 'My Policies', icon: '📄' },
    { to: '/my-claims', label: 'My Claims', icon: '🧾' },
    { to: '/my-premiums', label: 'My Premiums', icon: '💳' },
    { to: '/my-documents', label: 'My Documents', icon: '📁' },
  ],
};

export default function DashboardLayout() {
  const { user, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const navigate = useNavigate();
  const items = NAV_ITEMS[user?.role] || [];

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="flex min-h-screen bg-gray-50">
      <aside className="w-64 bg-brand-900 text-white flex flex-col shrink-0">
        <div className="px-6 py-5 border-b border-white/10">
          <h1 className="text-lg font-bold leading-tight">Insurance</h1>
          <p className="text-xs text-brand-200">Management Platform</p>
        </div>
        <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
          {items.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                  isActive ? 'bg-brand-500 text-white' : 'text-brand-100 hover:bg-white/10'
                }`
              }
            >
              <span>{item.icon}</span>
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="px-3 py-4 border-t border-white/10 space-y-1">
          <NavLink
            to="/profile"
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                isActive ? 'bg-brand-500 text-white' : 'text-brand-100 hover:bg-white/10'
              }`
            }
          >
            <span>👤</span> My Profile
          </NavLink>
          <button onClick={handleLogout} className="w-full text-left px-3 py-2.5 rounded-lg text-sm font-medium text-brand-100 hover:bg-white/10 transition-colors">
            🚪 Logout
          </button>
        </div>
      </aside>

      <div className="flex-1 flex flex-col min-w-0">
        <header className="bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
          <div>
            <p className="text-sm text-gray-400">Welcome back,</p>
            <p className="font-semibold text-gray-800">{user?.name}</p>
          </div>
          <div className="flex items-center gap-4">
            <button
              onClick={toggleTheme}
              title={theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
              className="w-9 h-9 rounded-full bg-gray-100 hover:bg-gray-200 flex items-center justify-center transition-colors text-lg"
            >
              {theme === 'dark' ? '☀️' : '🌙'}
            </button>
            <NotificationBell />
            <span className="badge bg-brand-100 text-brand-700">{user?.role}</span>
          </div>
        </header>
        <main className="flex-1 p-6 overflow-y-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
