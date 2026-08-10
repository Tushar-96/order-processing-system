import { Outlet } from "react-router-dom";

import Navbar from "../components/Navbar";
import "./AppLayout.css";

function AppLayout() {
  return (
    <div className="app-layout">
      <Navbar />

      <div className="app-content">
        <Outlet />
      </div>
    </div>
  );
}

export default AppLayout;