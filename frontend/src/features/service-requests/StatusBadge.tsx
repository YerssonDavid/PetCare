import { statusLabels, type ServiceRequest } from '../../shared/api/schemas';
import styles from '../../shared/ui/ui.module.css';

const classes = {
  PENDIENTE: styles.pending,
  ACEPTADO: styles.accepted,
  RECHAZADO: styles.rejected,
};
export function StatusBadge({ status }: { status: ServiceRequest['status'] }) {
  return <span className={`${styles.badge} ${classes[status]}`}>{statusLabels[status]}</span>;
}
