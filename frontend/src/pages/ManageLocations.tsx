import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
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
    <div className="max-w-2xl mx-auto mt-10 p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">Manage Departments & Locations</h1>
        <button onClick={() => navigate('/tickets')} className="text-sm text-blue-600 hover:underline">
          Back to Tickets
        </button>
      </div>

      {error && <p className="text-red-600 mb-4">{error}</p>}

      <div className="grid grid-cols-2 gap-6">
        <div>
          <h2 className="font-semibold mb-2">Departments</h2>
          <form onSubmit={handleCreateDept} className="flex gap-2 mb-3">
            <input
              type="text"
              placeholder="New department"
              value={deptName}
              onChange={(e) => setDeptName(e.target.value)}
              className="flex-1 border rounded px-2 py-1 text-sm"
            />
            <button type="submit" className="bg-blue-600 text-white px-3 py-1 rounded text-sm">
              Add
            </button>
          </form>
          <ul className="space-y-1">
            {departments.map((d) => (
              <li key={d.id} className="text-sm border rounded px-2 py-1 bg-white">
                {d.name}
              </li>
            ))}
          </ul>
        </div>

        <div>
          <h2 className="font-semibold mb-2">Locations</h2>
          <form onSubmit={handleCreateLoc} className="space-y-2 mb-3">
            <input
              type="text"
              placeholder="New location"
              value={locName}
              onChange={(e) => setLocName(e.target.value)}
              className="w-full border rounded px-2 py-1 text-sm"
            />
            <select
              value={locParentId}
              onChange={(e) => setLocParentId(e.target.value)}
              className="w-full border rounded px-2 py-1 text-sm"
            >
              <option value="">No parent (top-level)</option>
              {locations.map((l) => (
                <option key={l.id} value={l.id}>
                  {l.name}
                </option>
              ))}
            </select>
            <button type="submit" className="w-full bg-blue-600 text-white px-3 py-1 rounded text-sm">
              Add
            </button>
          </form>
          <ul className="space-y-1">
            {locations.map((l) => (
              <li key={l.id} className="text-sm border rounded px-2 py-1 bg-white">
                {l.name}
                {l.parentLocationId && (
                  <span className="text-gray-400">
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
  );
}