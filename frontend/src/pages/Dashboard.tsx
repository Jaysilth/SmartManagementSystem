import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, LayoutDashboard } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';
import api from '../lib/api';
import { getCurrentUser } from '../lib/auth';

interface DashboardData {
  openCount: number;
  byStatus: Record<string, number>;
}

export default function Dashboard() {
  const [data, setData] = useState<DashboardData | null>(null);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const currentUser = getCurrentUser();
  const isAllowed = currentUser?.role === 'ADMIN' || currentUser?.role === 'MANAGER';

  useEffect(() => {
    if (!isAllowed) return;
    api
      .get('/dashboard')
      .then((res) => setData(res.data))
      .catch(() => setError('Failed to load dashboard'));
  }, [isAllowed]);

  if (!isAllowed) {
    return (
      <div className="max-w-md mx-auto mt-10 p-6 text-center">
        <p className="text-red-600">You don't have permission to view this page.</p>
      </div>
    );
  }

  const chartData = data
    ? Object.entries(data.byStatus).map(([status, count]) => ({ status: status.replace('_', ' '), count }))
    : [];

  return (
    <div className="min-h-screen bg-[#fafafa] py-10">
      <div className="max-w-2xl mx-auto px-6">
        <button
          onClick={() => navigate('/tickets')}
          className="flex items-center gap-1 text-sm text-slate-500 hover:text-slate-700 mb-4"
        >
          <ArrowLeft size={16} />
          Back to Tickets
        </button>

        <div className="flex items-center gap-2 mb-1">
          <LayoutDashboard size={20} className="text-slate-700" />
          <h1 className="text-2xl font-semibold text-slate-800">Dashboard</h1>
        </div>
        <p
          className="text-xs uppercase tracking-widest text-slate-400 mb-6"
          style={{ fontFamily: "'JetBrains Mono', monospace" }}
        >
          SMMS · Operational overview
        </p>

        {error && (
          <p className="text-red-600 mb-4 text-sm border border-red-200 bg-red-50 rounded px-3 py-2">
            {error}
          </p>
        )}

        {data && (
          <>
            <div className="bg-white border-2 border-slate-800 rounded-lg p-6 shadow-[6px_6px_0px_0px_rgba(30,41,59,1)] mb-6">
              <p className="text-xs uppercase tracking-wide text-slate-500 mb-1">Open Requests</p>
              <p className="text-4xl font-bold text-slate-800">{data.openCount}</p>
            </div>

            <div className="bg-white border-2 border-slate-800 rounded-lg p-6 shadow-[6px_6px_0px_0px_rgba(30,41,59,1)]">
              <p className="text-xs uppercase tracking-wide text-slate-500 mb-4">Tickets by Status</p>
              <ResponsiveContainer width="100%" height={250}>
                <BarChart data={chartData}>
                  <XAxis dataKey="status" tick={{ fontSize: 12 }} />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Bar dataKey="count" fill="#1e293b" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </>
        )}
      </div>
    </div>
  );
}