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

export default function Tickets() {
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [technicians, setTechnicians] = useState<UserOption[]>([]);
  const [error, setError] = useState('');
  const [newTitle, setNewTitle] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();

  const currentUser = getCurrentUser();
  const canManageUsers = currentUser?.role === 'ADMIN' || currentUser?.role === 'MANAGER';

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
      await api.post('/tickets', { title: newTitle });
      setNewTitle('');
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
          <button onClick={handleLogout} className="text-sm text-red-600 hover:underline">
            Log out
          </button>
        </div>
      </div>

      <form onSubmit={handleCreate} className="flex gap-2 mb-6">
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
          </li>
        ))}
      </ul>
    </div>
  );
}