import { Link } from 'react-router-dom'
import type { ClothingItemResponse } from '../api/types'
import { FIT_LABELS } from '../data/clothingLabels'
import { formatLongDate } from '../lib/format'

/**
 * 옷장 화면의 옷 아이템 카드. 개별 아이템 크롭 사진이 없으면(imageUrl null)
 * 그 아이템이 나온 날의 OOTD 전체 사진(ootdPhotoUrl)으로 대체한다.
 * 탭하면 자동 인식 결과(카테고리/색상/핏)를 수정할 수 있는 화면으로 이동한다.
 */
export function ClothingItemCard({ item }: { item: ClothingItemResponse }) {
  const photoUrl = item.imageUrl ?? item.ootdPhotoUrl

  return (
    <li>
      <Link
        to={`/clothing-items/${item.id}`}
        className="block overflow-hidden rounded-2xl border border-line bg-paper"
      >
        <div className="relative aspect-square w-full bg-line">
          <img src={photoUrl} alt="옷 사진" loading="lazy" className="h-full w-full object-cover" />
          {item.fit && (
            <span className="absolute bottom-2 left-2 rounded-full bg-black/55 px-2 py-0.5 text-[11px] font-medium text-white backdrop-blur-sm">
              {FIT_LABELS[item.fit]}
            </span>
          )}
        </div>
        <div className="flex items-center gap-2 px-3 py-2.5">
          {item.color && (
            <span
              className="h-3.5 w-3.5 shrink-0 rounded-full border border-line"
              style={{ backgroundColor: item.color }}
              aria-hidden
            />
          )}
          <span className="truncate text-xs text-ink-soft">{formatLongDate(item.recordDate)}</span>
        </div>
      </Link>
    </li>
  )
}
