import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../lib/api';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const response = await api.post('/auth/login', { email, password });
      localStorage.setItem('token', response.data.token);
      navigate('/tickets');
    } catch {
      setError('Invalid email or password');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="flex items-center justify-center min-h-screen bg-[#fafafa]">
      <div className="w-80">
        <p
          className="text-center text-xs uppercase tracking-widest text-slate-400 mb-3"
          style={{ fontFamily: "'JetBrains Mono', monospace" }}
        >
          SMMS
        </p>

        <form
          onSubmit={handleSubmit}
          className="bg-white border-2 border-slate-800 rounded-lg p-8 shadow-[6px_6px_0px_0px_rgba(30,41,59,1)]"
        >
          <h1 className="text-2xl font-semibold text-slate-800 mb-1">Log in</h1>
          <p className="text-sm text-slate-500 mb-6">Sign in to manage your tickets</p>

          {error && (
            <p className="text-red-600 mb-4 text-sm border border-red-200 bg-red-50 rounded px-3 py-2">
              {error}
            </p>
          )}

          <label className="block text-xs uppercase tracking-wide text-slate-500 mb-1">
            Email
          </label>
          <input
            type="email"
            placeholder="you@company.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full border-2 border-slate-800 rounded px-3 py-2 mb-4 focus:outline-none focus:border-blue-600"
          />

          <label className="block text-xs uppercase tracking-wide text-slate-500 mb-1">
            Password
          </label>
          <input
            type="password"
            placeholder="••••••••"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full border-2 border-slate-800 rounded px-3 py-2 mb-6 focus:outline-none focus:border-blue-600"
          />

          <button
            type="submit"
            disabled={submitting}
            className="w-full bg-slate-800 text-white font-semibold py-2 rounded border-2 border-slate-800 shadow-[3px_3px_0px_0px_rgba(30,41,59,1)] hover:translate-x-[1px] hover:translate-y-[1px] hover:shadow-[2px_2px_0px_0px_rgba(30,41,59,1)] transition-transform disabled:opacity-50"
          >
            {submitting ? 'Signing in...' : 'Log in'}
          </button>

          <p className="text-sm text-center mt-4 text-slate-500">
            Need an account?{' '}
            <Link to="/register" className="text-blue-600 hover:underline">
              Register
            </Link>
          </p>
        </form>
      </div>
    </div>
  );
}