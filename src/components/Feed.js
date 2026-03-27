import React, { useState, useEffect, useCallback, useRef } from "react";
import FeedCard from "./FeedCard";

const ITEMS_PER_PAGE = 5;

function Feed({ votes, deputies }) {
  const [visibleItems, setVisibleItems] = useState(ITEMS_PER_PAGE);
  const [loading, setLoading] = useState(false);
  const observerRef = useRef(null);
  const sentinelRef = useRef(null);

  const deputyMap = {};
  for (const dep of deputies) {
    deputyMap[dep.id] = dep;
  }

  const loadMore = useCallback(() => {
    if (loading || visibleItems >= votes.length) return;

    setLoading(true);
    setTimeout(() => {
      setVisibleItems((prev) => Math.min(prev + ITEMS_PER_PAGE, votes.length));
      setLoading(false);
    }, 600);
  }, [loading, visibleItems, votes.length]);

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) {
          loadMore();
        }
      },
      { threshold: 0.1 }
    );

    observerRef.current = observer;

    if (sentinelRef.current) {
      observer.observe(sentinelRef.current);
    }

    return () => observer.disconnect();
  }, [loadMore]);

  useEffect(() => {
    setVisibleItems(ITEMS_PER_PAGE);
  }, [votes]);

  const visibleVotes = votes.slice(0, visibleItems);

  if (votes.length === 0) {
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

  return (
    <div className="feed">
      {visibleVotes.map((voteData) => (
        <FeedCard
          key={voteData.id}
          voteData={voteData}
          deputy={deputyMap[voteData.deputyId]}
        />
      ))}

      {visibleItems < votes.length && (
        <div ref={sentinelRef} className="feed-sentinel">
          {loading && (
            <div className="feed-loading">
              <div className="spinner" />
              <span>Carregando mais...</span>
            </div>
          )}
        </div>
      )}

      {visibleItems >= votes.length && votes.length > 0 && (
        <div className="feed-end">
          <span>✨ Você viu tudo por enquanto!</span>
        </div>
      )}
    </div>
  );
}

export default Feed;
