import React from "react";
import { Link } from "react-router-dom";

class NavBarItem extends React.Component {

    handleOnClick(text) {
        this.props.parentCallback(this.props.text)
    }

    render() {
        const navLink = "nav-link"
        var cssClass = this.props.isActive ? `${navLink} active` : navLink;
        var linkStyle = { "textTransform": "capitalize" };
        return (
            <li className="nav-item">
                <Link className={cssClass} to={this.props.link} style={linkStyle}
                    onClick={this.props.onClick}>
                    {this.props.text}
                </Link>
            </li>
        );
    }
}

export default NavBarItem