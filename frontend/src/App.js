import React, { useState, useEffect, useCallback, useMemo } from "react";
import Header from "./components/Header";
import StoriesBar from "./components/StoriesBar";
import Feed from "./components/Feed";
import FilterBar from "./components/FilterBar";
import FollowModal from "./components/FollowModal";
import { fetchDeputies, fetchFeed } from "./api";
import "./App.css";

const STORAGE_KEY = "deolhoneles_followed";
const FEED_PAGE_SIZE = 10;

function normalize(str) {
  return (str || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase();
}

function loadFollowedIds() {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    return stored ? JSON.parse(stored) : [];
  } catch {
    return [];
  }
}

function saveFollowedIds(ids) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(ids));
}

function App() {
  const [deputies, setDeputies] = useState([]);
  const [followedIds, setFollowedIds] = useState(loadFollowedIds);
  const [modalOpen, setModalOpen] = useState(false);
  const [categoryFilter, setCategoryFilter] = useState("Todos");
  const [deputyFilter, setDeputyFilter] = useState(null);

  const [feedItems, setFeedItems] = useState([]);
  const [feedPage, setFeedPage] = useState(0);
  const [feedHasMore, setFeedHasMore] = useState(false);
  const [feedLoading, setFeedLoading] = useState(false);

  // Load all deputies on mount
  useEffect(() => {
    fetchDeputies(0, 1000)
      .then((data) => setDeputies(data.content))
      .catch(console.error);
  }, []);

  // Deputies with follow state derived from localStorage
  const followedSet = useMemo(() => new Set(followedIds), [followedIds]);

  const deputiesWithFollow = useMemo(
    () =>
      deputies.map((d) => ({
        ...d,
        state: d.legend,
        following: followedSet.has(d.id),
      })),
    [deputies, followedSet],
  );

  // Stable key for feed query — changes when deputy selection changes
  const feedKey = useMemo(() => {
    if (deputyFilter) return `d:${deputyFilter}`;
    return `f:${[...followedIds].sort((a, b) => a - b).join(",")}`;
  }, [deputyFilter, followedIds]);

  // Fetch feed when deputy selection changes
  useEffect(() => {
    let cancelled = false;
    const ids = deputyFilter ? [deputyFilter] : [...followedIds];

    setFeedItems([]);
    setFeedPage(0);
    setFeedHasMore(false);

    if (ids.length === 0) return;

    setFeedLoading(true);
    fetchFeed(ids, 0, FEED_PAGE_SIZE)
      .then((data) => {
        if (cancelled) return;
        setFeedItems(data.content);
        setFeedHasMore(!data.last);
        setFeedPage(1);
      })
      .catch(console.error)
      .finally(() => {
        if (!cancelled) setFeedLoading(false);
      });

    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [feedKey]);

  // Load next page of feed
  const loadMoreFeed = useCallback(() => {
    if (feedLoading || !feedHasMore) return;

    const ids = deputyFilter ? [deputyFilter] : [...followedIds];
    if (ids.length === 0) return;

    setFeedLoading(true);
    fetchFeed(ids, feedPage, FEED_PAGE_SIZE)
      .then((data) => {
        setFeedItems((prev) => [...prev, ...data.content]);
        setFeedHasMore(!data.last);
        setFeedPage((prev) => prev + 1);
      })
      .catch(console.error)
      .finally(() => setFeedLoading(false));
  }, [feedLoading, feedHasMore, deputyFilter, followedIds, feedPage]);

  // Client-side category filter
  const filteredFeed = useMemo(() => {
    if (categoryFilter === "Todos") return feedItems;
    const target = normalize(categoryFilter);
    return feedItems.filter((item) => normalize(item.category) === target);
  }, [feedItems, categoryFilter]);

  const followed = deputiesWithFollow.filter((d) => d.following);

  function toggleFollow(id) {
    setFollowedIds((prev) => {
      const set = new Set(prev);
      if (set.has(id)) set.delete(id);
      else set.add(id);
      const next = [...set];
      saveFollowedIds(next);
      return next;
    });
  }

  return (
    <div className="app">
      <Header onOpenFollow={() => setModalOpen(true)} />
      <StoriesBar
        deputies={followed}
        activeDeputyId={deputyFilter}
        onSelectDeputy={(id) =>
          setDeputyFilter((prev) => (prev === id ? null : id))
        }
        onAddClick={() => setModalOpen(true)}
      />
      <FilterBar activeFilter={categoryFilter} onFilter={setCategoryFilter} />

      <main className="main">
        <Feed
          items={filteredFeed}
          hasMore={feedHasMore && categoryFilter === "Todos"}
          loading={feedLoading}
          onLoadMore={loadMoreFeed}
        />
      </main>

      {modalOpen && (
        <FollowModal
          deputies={deputiesWithFollow}
          onToggleFollow={toggleFollow}
          onClose={() => setModalOpen(false)}
        />
      )}
    </div>
  );
}

export default App;
