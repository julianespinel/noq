import React from "react";
import NavBarItem from "./NavBarItem";

import { withRouter } from 'react-router-dom';
import { ADMIN, AGENT, CUSTOMER, MANAGER } from "./Roles";

const LOGIN = "login";
const COMPANIES = "companies";
const BRANCHES = "branches";
const QUEUES = "queues";
const TURNS = "turns";
const CONFIRMATION = "confirmation";
const LOGOUT = "logout";

class MenuBar extends React.Component {

    getMenuItem(link, text, isActive) {
        return (
            <NavBarItem link={link} text={text} isActive={isActive} />
        )
    }

    getMenuForRole(role) {
        const currentLocation = this.props.location.pathname

        switch (role) {
            case null:
            case "":
                return this.getPublicMenu(currentLocation);
            case ADMIN:
                return this.getAdminMenu(currentLocation);
            case MANAGER:
                return this.getManagerMenu(currentLocation);
            case AGENT:
                return this.getAgentMenu(currentLocation);
            case CUSTOMER:
                return this.getCustomerMenu(currentLocation);
            default:
                throw new Error(`The given role is not valid: ${role}`);
        }
    }

    getPublicMenu(currentLocation) {
        return (
            <div className="container-fluid">
                <ul className="nav nav-pills">
                    {this.getMenuItem("/login", LOGIN, currentLocation.includes(LOGIN))}
                </ul>
            </div>
        );
    }

    getAdminMenu(currentLocation) {
        return (
            <div className="container-fluid">
                <ul className="nav nav-pills">
                    {this.getMenuItem("/companies", COMPANIES, currentLocation.includes(COMPANIES))}
                    {this.getMenuItem("/branches", BRANCHES, currentLocation.includes(BRANCHES))}
                    {this.getMenuItem("/queues", QUEUES, currentLocation.includes(QUEUES))}
                </ul>
                <ul className="nav justify-content-end nav-pills">
                    {this.getMenuItem("/logout", LOGOUT, currentLocation.includes(LOGOUT))}
                </ul>
            </div>
        );
    }

    getManagerMenu(currentLocation) {
        return (
            <div className="container-fluid">
                <ul className="nav nav-pills">
                    {this.getMenuItem("/branches", BRANCHES, currentLocation.includes(BRANCHES))}
                    {this.getMenuItem("/queues", QUEUES, currentLocation.includes(QUEUES))}
                    {this.getMenuItem("/agent", AGENT, currentLocation.includes(AGENT))}
                </ul>
                <ul className="nav justify-content-end nav-pills">
                    {this.getMenuItem("/logout", LOGOUT, currentLocation.includes(LOGOUT))}
                </ul>
            </div>
        );
    }

    getAgentMenu(currentLocation) {
        return (
            <div className="container-fluid">
                <ul className="nav nav-pills">
                    {this.getMenuItem("/agent", AGENT, currentLocation.includes(AGENT))}
                </ul>
                <ul className="nav justify-content-end nav-pills">
                    {this.getMenuItem("/logout", LOGOUT, currentLocation.includes(LOGOUT))}
                </ul>
            </div>
        );
    }

    getCustomerMenu(currentLocation) {
        return (
            <div className="container-fluid">
                <ul className="nav nav-pills">
                    {this.getMenuItem("/turns", TURNS, currentLocation.includes(TURNS))}
                </ul>
            </div>
        );
    }

    render() {
        const currentLocation = this.props.location.pathname;
        if (currentLocation === "/login") {
            localStorage.clear();
        }

        // Show menu bar only if we are not in the customer view
        let isPublicView = currentLocation.includes(LOGIN)
            || currentLocation.includes(TURNS)
            || currentLocation.includes(CONFIRMATION);
        let hide = isPublicView ? "d-none" : "";

        let role = localStorage.getItem("role");
        let menu = this.getMenuForRole(role);

        return (
            <nav className={`navbar fixed-top navbar-light ${hide}`}>
                {menu}
            </nav>
        )
    }
}

export default withRouter(MenuBar);