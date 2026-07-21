import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../lib/api';
import { getCurrentUser } from '../lib/auth';

interface Ticket {
  id: number;
  organizationId: number;
  title: string;
  status: string;
  assignedTechnicianId: number | null;
  createdBy: number | null;
}

interface UserOption {
  id: number;
  email: string;
  role: string;
}

interface Department {
  id: number;
  name: string;
}

interface LocationOption {
  id: number;
  name: string;
}

export default function Tickets() {
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [technicians, setTechnicians] = useState<UserOption[]>([]);
  const [error, setError] = useState('');
  const [newTitle, setNewTitle] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [locations, setLocations] = useState<LocationOption[]>([]);
  const [selectedDept, setSelectedDept] = useState('');
  const [selectedLoc, setSelectedLoc] = useState('');
  const navigate = useNavigate();

  const currentUser = getCurrentUser();
  const canManageUsers = currentUser?.role === 'ADMIN' || currentUser?.role === 'MANAGER';

  const isTechnician = currentUser?.role === 'TECHNICIAN';
  const currentUserId = currentUser ? Number(currentUser.sub) : null;
  const isRequester = currentUser?.role === 'REQUESTER';

  

  function loadTickets() {
    api
      .get('/tickets')
      .then((response) => setTickets(response.data))
      .catch(() => setError('Failed to load tickets'));
  }

  useEffect(() => {
    loadTickets();
  }, []);

  useEffect(() => {
  api.get('/departments').then((res) => setDepartments(res.data)).catch(() => {});
  api.get('/locations').then((res) => setLocations(res.data)).catch(() => {});
}, []);

  useEffect(() => {
    if (canManageUsers) {
      api
        .get('/auth/users?role=TECHNICIAN')
        .then((response) => setTechnicians(response.data))
        .catch(() => {});
    }
  }, [canManageUsers]);

  async function handleCreate(e: React.FormEvent) {
  e.preventDefault();
  if (!newTitle.trim()) return;

  setSubmitting(true);
  try {
    await api.post('/tickets', {
      title: newTitle,
      departmentId: selectedDept ? Number(selectedDept) : null,
      locationId: selectedLoc ? Number(selectedLoc) : null,
    });
    setNewTitle('');
    setSelectedDept('');
    setSelectedLoc('');
    loadTickets();
  } catch {
    setError('Failed to create ticket');
  } finally {
    setSubmitting(false);
  }
}

  async function handleAssign(ticketId: number, technicianId: number) {
    try {
      await api.patch(`/tickets/${ticketId}/assign`, { technicianId });
      loadTickets();
    } catch {
      setError('Failed to assign ticket');
    }
  }

  async function handleStatusChange(ticketId: number, status: string) {
  try {
    await api.patch(`/tickets/${ticketId}/status`, { status });
    loadTickets();
  } catch {
    setError('Failed to update status');
  }
}

  function handleLogout() {
    localStorage.removeItem('token');
    navigate('/login');
  }

  return (
    <div className="max-w-2xl mx-auto mt-10 p-6">
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-2xl font-bold">Tickets</h1>

        <div className="flex items-center gap-4">
          {canManageUsers && (
            <button
              onClick={() => navigate('/create-user')}
              className="text-sm text-blue-600 hover:underline"
            >
              Create User
            </button>
          )}
          {canManageUsers && (
  <button
    onClick={() => navigate('/manage-locations')}
    className="text-sm text-blue-600 hover:underline"
  >
    Manage Locations
  </button>
)}
          <button onClick={handleLogout} className="text-sm text-red-600 hover:underline">
            Log out
          </button>
        </div>
      </div>

      <form onSubmit={handleCreate} className="space-y-2 mb-6">
  <div className="flex gap-2">
    <input
      type="text"
      placeholder="New ticket title"
      value={newTitle}
      onChange={(e) => setNewTitle(e.target.value)}
      className="flex-1 border rounded px-3 py-2"
    />
    <button
      type="submit"
      disabled={submitting}
      className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 disabled:opacity-50"
    >
      {submitting ? 'Adding...' : 'Add'}
    </button>
  </div>
  <div className="flex gap-2">
    <select
      value={selectedDept}
      onChange={(e) => setSelectedDept(e.target.value)}
      className="flex-1 border rounded px-2 py-1 text-sm"
    >
      <option value="">No department</option>
      {departments.map((d) => (
        <option key={d.id} value={d.id}>
          {d.name}
        </option>
      ))}
    </select>
    <select
      value={selectedLoc}
      onChange={(e) => setSelectedLoc(e.target.value)}
      className="flex-1 border rounded px-2 py-1 text-sm"
    >
      <option value="">No location</option>
      {locations.map((l) => (
        <option key={l.id} value={l.id}>
          {l.name}
        </option>
      ))}
    </select>
  </div>
</form>

      {error && <p className="text-red-600 mb-3">{error}</p>}

      <ul className="space-y-2">
        {tickets.map((ticket) => (
          <li key={ticket.id} className="border rounded p-3 bg-white shadow-sm">
            <div className="flex justify-between items-center">
              <span>{ticket.title}</span>
              <span className="text-xs font-mono text-gray-500">{ticket.status}</span>
            </div>

            {canManageUsers && (
              <div className="mt-2">
                <select
                  value={ticket.assignedTechnicianId ?? ''}
                  onChange={(e) => handleAssign(ticket.id, Number(e.target.value))}
                  className="text-sm border rounded px-2 py-1"
                >
                  <option value="" disabled>
                    Assign technician
                  </option>
                  {technicians.map((tech) => (
                    <option key={tech.id} value={tech.id}>
                      {tech.email}
                    </option>
                  ))}
                </select>
              </div>
            )}
            {isTechnician && ticket.assignedTechnicianId === currentUserId && (
  <div className="mt-2 flex gap-2">
    {['ACCEPTED', 'IN_PROGRESS', 'WAITING_FOR_PARTS', 'COMPLETED'].map((s) => (
      <button
        key={s}
        onClick={() => handleStatusChange(ticket.id, s)}
        disabled={ticket.status === s}
        className="text-xs border rounded px-2 py-1 hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed"
      >
        {s.replace('_', ' ')}
      </button>
    ))}
  </div>
)}
{isRequester && ticket.createdBy === currentUserId && (
  <div className="mt-2 flex gap-2">
    {ticket.status === 'COMPLETED' && (
      <button
        onClick={() => handleStatusChange(ticket.id, 'REOPENED')}
        className="text-xs border rounded px-2 py-1 hover:bg-gray-100"
      >
        Reopen
      </button>
    )}
    {!['CLOSED', 'CANCELLED'].includes(ticket.status) && (
      <>
        <button
          onClick={() => handleStatusChange(ticket.id, 'CLOSED')}
          className="text-xs border rounded px-2 py-1 hover:bg-gray-100"
        >
          Close
        </button>
        <button
          onClick={() => handleStatusChange(ticket.id, 'CANCELLED')}
          className="text-xs border rounded px-2 py-1 hover:bg-gray-100"
        >
          Cancel
        </button>
      </>
    )}
  </div>
)}
          </li>
        ))}
      </ul>
    </div>
  );
}