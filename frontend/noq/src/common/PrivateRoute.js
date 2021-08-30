import React from "react";
import { Redirect, Route } from "react-router-dom";

class PrivateRoute extends React.Component {

    getComponentToRender(isAuthorized) {
        if (isAuthorized) {
            return <this.props.component {...this.props} />;
        }

        localStorage.removeItem("role");
        return <Redirect to="/login" />;
    }

    render() {
        let role = localStorage.getItem("role");
        let isAuthorized = role && this.props.roles.includes(role);

        let toRender = this.getComponentToRender(isAuthorized);
        let { component, ...rest } = this.props;

        return (
            <Route {...rest} render={() => toRender} />
        );
    }
}

export default PrivateRoute