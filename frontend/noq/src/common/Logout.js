import React from "react";

import { withRouter, Redirect } from 'react-router-dom';

class Logout extends React.Component {

    constructor(props) {
        super(props);
        localStorage.clear();
    }

    render() {
        return <Redirect to="/login" />;
    }
}

export default withRouter(Logout);
