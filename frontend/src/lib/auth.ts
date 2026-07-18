import { jwtDecode } from 'jwt-decode';

interface TokenPayload {
  sub: string;
  organizationId: number;
  role: string;
  iat: number;
  exp: number;
}

export function getCurrentUser(): TokenPayload | null {
  const token = localStorage.getItem('token');
  if (!token) return null;
  try {
    return jwtDecode<TokenPayload>(token);
  } catch {
    return null;
  }
}