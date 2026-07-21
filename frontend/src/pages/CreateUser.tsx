import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import api from '../lib/api';
import { getCurrentUser } from '../lib/auth';

export default function CreateUser() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('REQUESTER');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();

  const currentUser = getCurrentUser();
  const isAllowed = currentUser?.role === 'ADMIN' || currentUser?.role === 'MANAGER';

  if (!isAllowed) {
    return (
      <div className="max-w-md mx-auto mt-10 p-6 text-center">
        <p className="text-red-600">You don't have permission to view this page.</p>
      </div>
    );
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setSuccess('');
    setSubmitting(true);
    try {
      await api.post('/auth/users', { email, password, role });
      setSuccess(`User ${email} created as ${role}`);
      setEmail('');
      setPassword('');
      setRole('REQUESTER');
    } catch (err: any) {
      setError(err.response?.data?.error ?? 'Failed to create user');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="min-h-screen bg-[#fafafa] py-10">
      <div className="max-w-md mx-auto px-6">
        <button
          onClick={() => navigate('/tickets')}
          className="flex items-center gap-1 text-sm text-slate-500 hover:text-slate-700 mb-4"
        >
          <ArrowLeft size={16} />
          Back to Tickets
        </button>

        <p
          className="text-xs uppercase tracking-widest text-slate-400 mb-3"
          style={{ fontFamily: "'JetBrains Mono', monospace" }}
        >
          SMMS · User management
        </p>

        <form
          onSubmit={handleSubmit}
          className="bg-white border-2 border-slate-800 rounded-lg p-8 shadow-[6px_6px_0px_0px_rgba(30,41,59,1)]"
        >
          <h1 className="text-2xl font-semibold text-slate-800 mb-1">Create user</h1>
          <p className="text-sm text-slate-500 mb-6">Add a new team member to your organization</p>

          {error && (
            <p className="text-red-600 mb-4 text-sm border border-red-200 bg-red-50 rounded px-3 py-2">
              {error}
            </p>
          )}
          {success && (
            <p className="text-green-700 mb-4 text-sm border border-green-200 bg-green-50 rounded px-3 py-2">
              {success}
            </p>
          )}

          <label className="block text-xs uppercase tracking-wide text-slate-500 mb-1">
            Email
          </label>
          <input
            type="email"
            placeholder="teammate@company.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full border-2 border-slate-800 rounded px-3 py-2 mb-4 focus:outline-none focus:border-blue-600"
          />

          <label className="block text-xs uppercase tracking-wide text-slate-500 mb-1">
            Temporary password
          </label>
          <input
            type="password"
            placeholder="••••••••"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full border-2 border-slate-800 rounded px-3 py-2 mb-4 focus:outline-none focus:border-blue-600"
          />

          <label className="block text-xs uppercase tracking-wide text-slate-500 mb-1">
            Role
          </label>
          <select
            value={role}
            onChange={(e) => setRole(e.target.value)}
            className="w-full border-2 border-slate-800 rounded px-3 py-2 mb-6 focus:outline-none focus:border-blue-600"
          >
            <option value="REQUESTER">Requester</option>
            <option value="TECHNICIAN">Technician</option>
            <option value="MANAGER">Manager</option>
            <option value="ADMIN">Admin</option>
          </select>

          <button
            type="submit"
            disabled={submitting}
            className="w-full bg-slate-800 text-white font-semibold py-2 rounded border-2 border-slate-800 shadow-[3px_3px_0px_0px_rgba(30,41,59,1)] hover:translate-x-[1px] hover:translate-y-[1px] hover:shadow-[2px_2px_0px_0px_rgba(30,41,59,1)] transition-transform disabled:opacity-50"
          >
            {submitting ? 'Creating...' : 'Create user'}
          </button>
        </form>
      </div>
    </div>
  );
}