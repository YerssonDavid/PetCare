import logo from '../../../../img/petCareLogo.png';
import styles from './ui.module.css';

export function Brand({ compact = false }: { compact?: boolean }) {
  return (
    <span className={styles.brand}>
      <img src={logo} alt="" width="38" height="38" />
      {!compact && (
        <span>
          PetCare<span className={styles.brandCaption}>CUIDADO QUE CONECTA</span>
        </span>
      )}
    </span>
  );
}
