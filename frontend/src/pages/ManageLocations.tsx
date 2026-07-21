import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Building2, MapPin } from 'lucide-react';
import api from '../lib/api';
import { getCurrentUser } from '../lib/auth';

interface Department {
  id: number;
  name: string;
}

interface Location {
  id: number;
  name: string;
  parentLocationId: number | null;
}

export default function ManageLocations() {
  const [departments, setDepartments] = useState<Department[]>([]);
  const [locations, setLocations] = useState<Location[]>([]);
  const [deptName, setDeptName] = useState('');
  const [locName, setLocName] = useState('');
  const [locParentId, setLocParentId] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const currentUser = getCurrentUser();
  const canManage = currentUser?.role === 'ADMIN' || currentUser?.role === 'MANAGER';

  function loadData() {
    api.get('/departments').then((res) => setDepartments(res.data)).catch(() => {});
    api.get('/locations').then((res) => setLocations(res.data)).catch(() => {});
  }

  useEffect(() => {
    loadData();
  }, []);

  if (!canManage) {
    return (
      <div className="max-w-md mx-auto mt-10 p-6 text-center">
        <p className="text-red-600">You don't have permission to view this page.</p>
      </div>
    );
  }

  async function handleCreateDept(e: React.FormEvent) {
    e.preventDefault();
    if (!deptName.trim()) return;
    try {
      await api.post('/departments', { name: deptName });
      setDeptName('');
      loadData();
    } catch {
      setError('Failed to create department');
    }
  }

  async function handleCreateLoc(e: React.FormEvent) {
    e.preventDefault();
    if (!locName.trim()) return;
    try {
      await api.post('/locations', {
        name: locName,
        parentLocationId: locParentId ? Number(locParentId) : null,
      });
      setLocName('');
      setLocParentId('');
      loadData();
    } catch {
      setError('Failed to create location');
    }
  }

  return (
    <div className="min-h-screen bg-[#fafafa] py-10">
      <div className="max-w-3xl mx-auto px-6">
        <button
          onClick={() => navigate('/tickets')}
          className="flex items-center gap-1 text-sm text-slate-500 hover:text-slate-700 mb-4"
        >
          <ArrowLeft size={16} />
          Back to Tickets
        </button>

        <div className="flex justify-between items-center mb-1">
          <h1 className="text-2xl font-semibold text-slate-800">Departments & Locations</h1>
        </div>
        <p
          className="text-xs uppercase tracking-widest text-slate-400 mb-6"
          style={{ fontFamily: "'JetBrains Mono', monospace" }}
        >
          SMMS · Facility management
        </p>

        {error && (
          <p className="text-red-600 mb-4 text-sm border border-red-200 bg-red-50 rounded px-3 py-2">
            {error}
          </p>
        )}

        <div className="grid grid-cols-2 gap-6">
          <div className="bg-white border-2 border-slate-800 rounded-lg p-6 shadow-[6px_6px_0px_0px_rgba(30,41,59,1)]">
            <div className="flex items-center gap-2 mb-4">
              <Building2 size={18} className="text-slate-700" />
              <h2 className="font-semibold text-slate-800">Departments</h2>
            </div>

            <form onSubmit={handleCreateDept} className="space-y-2 mb-4">
              <input
                type="text"
                placeholder="New department"
                value={deptName}
                onChange={(e) => setDeptName(e.target.value)}
                className="w-full border-2 border-slate-800 rounded px-2 py-1 text-sm focus:outline-none focus:border-blue-600"
              />
              <button
                type="submit"
                className="w-full bg-slate-800 text-white px-3 py-1 rounded border-2 border-slate-800 text-sm shadow-[2px_2px_0px_0px_rgba(30,41,59,1)] hover:translate-x-[1px] hover:translate-y-[1px] hover:shadow-none transition-transform"
              >
                Add
              </button>
            </form>

            <ul className="space-y-1">
              {departments.map((d) => (
                <li
                  key={d.id}
                  className="text-sm border border-slate-200 rounded px-2 py-1 bg-slate-50"
                >
                  {d.name}
                </li>
              ))}
            </ul>
          </div>

          <div className="bg-white border-2 border-slate-800 rounded-lg p-6 shadow-[6px_6px_0px_0px_rgba(30,41,59,1)]">
            <div className="flex items-center gap-2 mb-4">
              <MapPin size={18} className="text-slate-700" />
              <h2 className="font-semibold text-slate-800">Locations</h2>
            </div>

            <form onSubmit={handleCreateLoc} className="space-y-2 mb-4">
              <input
                type="text"
                placeholder="New location"
                value={locName}
                onChange={(e) => setLocName(e.target.value)}
                className="w-full border-2 border-slate-800 rounded px-2 py-1 text-sm focus:outline-none focus:border-blue-600"
              />
              <select
                value={locParentId}
                onChange={(e) => setLocParentId(e.target.value)}
                className="w-full border-2 border-slate-800 rounded px-2 py-1 text-sm focus:outline-none focus:border-blue-600"
              >
                <option value="">No parent (top-level)</option>
                {locations.map((l) => (
                  <option key={l.id} value={l.id}>
                    {l.name}
                  </option>
                ))}
              </select>
              <button
                type="submit"
                className="w-full bg-slate-800 text-white px-3 py-1 rounded border-2 border-slate-800 text-sm shadow-[2px_2px_0px_0px_rgba(30,41,59,1)] hover:translate-x-[1px] hover:translate-y-[1px] hover:shadow-none transition-transform"
              >
                Add
              </button>
            </form>

            <ul className="space-y-1">
              {locations.map((l) => (
                <li
                  key={l.id}
                  className="text-sm border border-slate-200 rounded px-2 py-1 bg-slate-50"
                >
                  {l.name}
                  {l.parentLocationId && (
                    <span className="text-slate-400">
                      {' '}
                      (under {locations.find((p) => p.id === l.parentLocationId)?.name ?? '?'})
                    </span>
                  )}
                </li>
              ))}
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}