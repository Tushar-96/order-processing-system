import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
 
import api from "../api/axiosInstance";
 
const AuthContext = createContext(null);
 
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
 
  useEffect(() => {
    let isMounted = true;
 
    async function restoreSession() {
      const token = localStorage.getItem("accessToken");
 
      if (!token) {
        if (isMounted) {
          setIsLoading(false);
        }
 
        return;
      }
 
      try {
        const response = await api.get("/api/v1/auth/me");
 
        if (isMounted) {
          setUser(response.data);
        }
      } catch {
        localStorage.removeItem("accessToken");
 
        if (isMounted) {
          setUser(null);
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }
 
    restoreSession();
 
    return () => {
      isMounted = false;
    };
  }, []);
 
  async function register(registrationData) {
    const response = await api.post(
      "/api/v1/auth/register",
      registrationData,
    );
 
    const authData = response.data;
 
    localStorage.setItem("accessToken", authData.accessToken);
 
    setUser({
      userId: authData.userId,
      email: authData.email,
      fullName: authData.fullName,
      role: authData.role,
      authorities: [authData.role],
    });
 
    return authData;
  }
 
  async function login(credentials) {
    const response = await api.post(
      "/api/v1/auth/login",
      credentials,
    );
 
    const authData = response.data;
 
    localStorage.setItem("accessToken", authData.accessToken);
 
    setUser({
      userId: authData.userId,
      email: authData.email,
      fullName: authData.fullName,
      role: authData.role,
      authorities: [authData.role],
    });
 
    return authData;
  }
 
  function logout() {
    localStorage.removeItem("accessToken");
    setUser(null);
  }
 
  const contextValue = useMemo(
    () => ({
      user,
      isLoading,
      isAuthenticated: Boolean(user),
      register,
      login,
      logout,
    }),
    [user, isLoading],
  );
 
  return (
<AuthContext.Provider value={contextValue}>
      {children}
</AuthContext.Provider>
  );
}
 
export function useAuth() {
  const context = useContext(AuthContext);
 
  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }
 
  return context;
}