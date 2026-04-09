import { createContext, useState, useContext, useEffect } from 'react'

const AuthContext = createContext()

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  // Cargar usuario del localStorage al iniciar
  useEffect(() => {
    const savedUser = localStorage.getItem('donatrack_user')
    if (savedUser) {
      try {
        setUser(JSON.parse(savedUser))
      } catch (error) {
        console.error('Error loading user:', error)
      }
    }
    setLoading(false)
  }, [])

  const login = (userData) => {
    const userWithTimestamp = {
      ...userData,
      loginAt: new Date().toISOString()
    }
    setUser(userWithTimestamp)
    localStorage.setItem('donatrack_user', JSON.stringify(userWithTimestamp))
  }

  const logout = () => {
    setUser(null)
    localStorage.removeItem('donatrack_user')
  }

  const updateUser = (updates) => {
    const updatedUser = { ...user, ...updates }
    setUser(updatedUser)
    localStorage.setItem('donatrack_user', JSON.stringify(updatedUser))
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, updateUser }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth debe ser usado dentro de AuthProvider')
  }
  return context
}
