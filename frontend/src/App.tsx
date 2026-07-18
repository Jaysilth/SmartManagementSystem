import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Tickets from './pages/Tickets';
import ProtectedRoute from './components/ProtectedRoute';
import Register from './pages/Register';
import CreateUser from './pages/CreateUser';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/tickets"
          element={
            <ProtectedRoute>
              <Tickets />
            </ProtectedRoute>
          }
        />
        <Route
  path="/create-user"
  element={
    <ProtectedRoute>
      <CreateUser />
    </ProtectedRoute>
  }
/>
        
<Route path="/register" element={<Register />} />
        <Route path="*" element={<Navigate to="/login" />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;