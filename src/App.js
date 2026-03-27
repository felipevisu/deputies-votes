import React, { useState, useMemo } from "react";
import Header from "./components/Header";
import StoriesBar from "./components/StoriesBar";
import Feed from "./components/Feed";
import FilterBar from "./components/FilterBar";
import FollowModal from "./components/FollowModal";
import deputiesData from "./data/deputies";
import { generateVotes } from "./data/votes";
import "./App.css";

function App() {
  const [deputies, setDeputies] = useState(deputiesData);
  const [modalOpen, setModalOpen] = useState(false);
  const [categoryFilter, setCategoryFilter] = useState("Todos");
  const [deputyFilter, setDeputyFilter] = useState(null);
  const votes = useMemo(() => generateVotes(deputies), [deputies]);

  const filteredVotes = useMemo(() => {
    const followedIds = new Set(
      deputies.filter((d) => d.following).map((d) => d.id),
    );
    let result = votes.filter((v) => followedIds.has(v.deputyId));
    if (deputyFilter) {
      result = result.filter((v) => v.deputyId === deputyFilter);
    }
    if (categoryFilter !== "Todos") {
      result = result.filter((v) => v.project.category === categoryFilter);
    }
    result.sort(
      (a, b) => new Date(b.project.voteDate) - new Date(a.project.voteDate),
    );
    return result;
  }, [votes, deputies, categoryFilter, deputyFilter]);

  const followed = deputies.filter((d) => d.following);

  function toggleFollow(id) {
    setDeputies((prev) =>
      prev.map((d) => (d.id === id ? { ...d, following: !d.following } : d)),
    );
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
        <Feed votes={filteredVotes} deputies={deputies} />
      </main>

      {modalOpen && (
        <FollowModal
          deputies={deputies}
          onToggleFollow={toggleFollow}
          onClose={() => setModalOpen(false)}
        />
      )}
    </div>
  );
}

export default App;
