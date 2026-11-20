import logo from '../../../../img/petCareLogo.png';
import styles from './ui.module.css';

export function Brand() {
  return (
    <span className={styles.brand}>
      <img src={logo} alt="" width="38" height="38" />
      <span>
        PetCare<span className={styles.brandCaption}>CUIDADO QUE CONECTA</span>
      </span>
    </span>
  );
}
