import React, { useEffect, useRef } from "react";
import FeedCard from "./FeedCard";
import ProposalFeedCard from "./ProposalFeedCard";

function Feed({ items, hasMore, loading, onLoadMore, feedMode }) {
  const observerRef = useRef(null);
  const sentinelRef = useRef(null);

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) {
          onLoadMore();
        }
      },
      { threshold: 0.1 },
    );

    observerRef.current = observer;

    if (sentinelRef.current) {
      observer.observe(sentinelRef.current);
    }

    return () => observer.disconnect();
  }, [onLoadMore]);

  if (items.length === 0 && !loading) {
    return (
      <div className="feed-empty">
        <div className="feed-empty-icon">🔍</div>
        <h3>Cadê os deputados?</h3>
        <p>
          Toca no <strong>+</strong> pra seguir seus deputados e ficar de olho
          no que eles andam votando!
        </p>
      </div>
    );
  }

  const CardComponent = feedMode === "proposals" ? ProposalFeedCard : FeedCard;
  const keyFn =
    feedMode === "proposals"
      ? (item, idx) => `p-${item.proposalId}-${idx}`
      : (item, idx) => `${item.id}-${item.deputyId}-${idx}`;

  return (
    <div className="feed">
      {items.map((item, idx) => (
        <CardComponent key={keyFn(item, idx)} item={item} />
      ))}

      {hasMore && (
        <div ref={sentinelRef} className="feed-sentinel">
          {loading && (
            <div className="feed-loading">
              <div className="spinner" />
              <span>Carregando mais...</span>
            </div>
          )}
        </div>
      )}

      {!hasMore && items.length > 0 && !loading && (
        <div className="feed-end">
          <span>✨ Você viu tudo por enquanto!</span>
        </div>
      )}
    </div>
  );
}

export default Feed;
