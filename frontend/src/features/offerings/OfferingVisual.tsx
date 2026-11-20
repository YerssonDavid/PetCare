import { useState } from 'react';
import { Icon } from '../../shared/ui/Icon';
import styles from './Offerings.module.css';

export function OfferingVisual({
  imageUrl,
  product,
  large = false,
}: {
  imageUrl?: string | null;
  product: boolean;
  large?: boolean;
}) {
  const [failedUrl, setFailedUrl] = useState<string | null>(null);
  const safe = imageUrl && /^(https?:\/\/|\/(?!\/))/.test(imageUrl) && failedUrl !== imageUrl;
  return (
    <div className={`${styles.visual} ${large ? styles.detailVisual : ''}`}>
      {safe ? (
        <img
          src={imageUrl}
          alt=""
          loading="lazy"
          referrerPolicy="no-referrer"
          onError={() => setFailedUrl(imageUrl)}
        />
      ) : (
        <Icon name={product ? 'bag' : 'paw'} size={large ? 72 : 54} />
      )}
      <span>{product ? 'Producto' : 'Servicio'}</span>
    </div>
  );
}
