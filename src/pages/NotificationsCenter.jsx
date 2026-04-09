import { useState } from 'react'
import { Bell, Check, Trash2 } from 'lucide-react'
import '../styles/pages/notifications-center.css'

export function NotificationsCenter() {
  const [notifications, setNotifications] = useState(getNotifications())
  const [filter, setFilter] = useState('all')

  function getNotifications() {
    return [
      {
        id: 'notif-1',
        type: 'donation_assigned',
        title: 'Donación Asignada',
        message: 'Tu donación de alimentos ha sido asignada a Fundación Despierta',
        timestamp: '2025-10-15T14:30:00',
        read: false,
        icon: '📦'
      },
      {
        id: 'notif-2',
        type: 'mission_completed',
        title: 'Misión Completada',
        message: 'Felicidades, completaste la misión "Donante Generoso"',
        timestamp: '2025-10-14T10:15:00',
        read: false,
        icon: '🎉'
      },
      {
        id: 'notif-3',
        type: 'category_upgraded',
        title: 'Cambio de Categoría',
        message: 'Subiste a la categoría Platino',
        timestamp: '2025-10-13T16:45:00',
        read: true,
        icon: '👑'
      },
      {
        id: 'notif-4',
        type: 'donation_received',
        title: 'Donación Recibida',
        message: 'Red Solidaria Norte confirmó la recepción de tu donación de abrigo',
        timestamp: '2025-10-12T09:20:00',
        read: true,
        icon: '✅'
      },
      {
        id: 'notif-5',
        type: 'delivery_update',
        title: 'Actualización de Entrega',
        message: 'El camión de tu donación está a 2.5 km de distancia',
        timestamp: '2025-10-15T15:00:00',
        read: false,
        icon: '🚚'
      }
    ]
  }

  const filteredNotifications = filter === 'all' 
    ? notifications
    : filter === 'unread'
    ? notifications.filter(n => !n.read)
    : notifications.filter(n => n.read)

  const handleMarkAsRead = (id) => {
    setNotifications(notifications.map(n => 
      n.id === id ? { ...n, read: true } : n
    ))
  }

  const handleDelete = (id) => {
    setNotifications(notifications.filter(n => n.id !== id))
  }

  const handleMarkAllAsRead = () => {
    setNotifications(notifications.map(n => ({ ...n, read: true })))
  }

  const unreadCount = notifications.filter(n => !n.read).length

  const getNotificationColor = (type) => {
    const colors = {
      donation_assigned: '#3498db',
      mission_completed: '#2ecc71',
      category_upgraded: '#f39c12',
      donation_received: '#27ae60',
      delivery_update: '#e74c3c'
    }
    return colors[type] || '#95a5a6'
  }

  return (
    <div className="notifications-center">
      <div className="notifications-header">
        <div className="header-title">
          <Bell size={24} />
          <div>
            <h2>Centro de Notificaciones</h2>
            <p>{unreadCount} notificaciones sin leer</p>
          </div>
        </div>
        {unreadCount > 0 && (
          <button className="btn-mark-all" onClick={handleMarkAllAsRead}>
            Marcar todas como leídas
          </button>
        )}
      </div>

      {/* Filter Tabs */}
      <div className="notification-filters">
        <button 
          className={`filter-tab ${filter === 'all' ? 'active' : ''}`}
          onClick={() => setFilter('all')}
        >
          Todas ({notifications.length})
        </button>
        <button 
          className={`filter-tab ${filter === 'unread' ? 'active' : ''}`}
          onClick={() => setFilter('unread')}
        >
          Sin leer ({unreadCount})
        </button>
        <button 
          className={`filter-tab ${filter === 'read' ? 'active' : ''}`}
          onClick={() => setFilter('read')}
        >
          Leídas ({notifications.filter(n => n.read).length})
        </button>
      </div>

      {/* Notifications List */}
      <div className="notifications-list">
        {filteredNotifications.length > 0 ? (
          filteredNotifications.map(notif => (
            <div 
              key={notif.id} 
              className={`notification-item ${!notif.read ? 'unread' : ''}`}
              style={{ borderLeftColor: getNotificationColor(notif.type) }}
            >
              <div className="notification-icon">
                {notif.icon}
              </div>
              <div className="notification-content">
                <h4>{notif.title}</h4>
                <p>{notif.message}</p>
                <small className="notification-time">
                  {formatTime(notif.timestamp)}
                </small>
              </div>
              <div className="notification-actions">
                {!notif.read && (
                  <button 
                    className="action-btn read-btn"
                    onClick={() => handleMarkAsRead(notif.id)}
                    title="Marcar como leída"
                  >
                    <Check size={18} />
                  </button>
                )}
                <button 
                  className="action-btn delete-btn"
                  onClick={() => handleDelete(notif.id)}
                  title="Eliminar"
                >
                  <Trash2 size={18} />
                </button>
              </div>
            </div>
          ))
        ) : (
          <div className="empty-state">
            <Bell size={48} />
            <h3>No hay notificaciones</h3>
            <p>No tienes notificaciones en este momento</p>
          </div>
        )}
      </div>
    </div>
  )
}

function formatTime(timestamp) {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (seconds < 60) return 'Hace unos segundos'
  if (minutes < 60) return `Hace ${minutes} min`
  if (hours < 24) return `Hace ${hours} h`
  if (days < 7) return `Hace ${days} días`
  
  return date.toLocaleDateString('es-AR')
}
