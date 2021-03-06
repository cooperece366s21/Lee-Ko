import React from "react";
import UserFeed from "./UserFeed";
import UserHeader from "./UserHeader";
import api from "../../services/api";
import {Center, Text} from "@chakra-ui/react";

// The purpose of this class is to have a class component that does basic API calls (Get user ID, other user things)
// and then feeds down the information down to more basic rendering components

type PassdownStates = {
    uid : number,
    cuid: number | null,
    name : string | null,
    exists : boolean | null,  // Null state for rendering blank when not yet explicitly set to boolean
    hasOwnership : boolean | null,
    bio : string | null,
    profilePicture: string | null,
    subscribers: number,
}

class UserPageWrapper extends React.Component<any, any> {

    state: PassdownStates = {
        cuid: -1,
        uid: -1,
        name: "",
        exists: null,
        hasOwnership: null,
        bio: null,
        profilePicture: null,
        subscribers: 0,
    }

    constructor(props:any) {
        super(props);
        this.state = {
            cuid: null,
            uid : props.uid,
            name : null,
            exists: null,
            hasOwnership: null,
            bio: null,
            profilePicture: null,
            subscribers: 0,
        }
    }

    componentDidMount() {

        api.getCurrentUserID()
            .then( cuid => {
                cuid === parseInt(String(this.state.uid)) ?
                    this.setState({hasOwnership:true, cuid:cuid}) :
                    this.setState({hasOwnership:false, cuid:cuid});

                api.getUser(this.state.uid)
                    .then( user => {

                        if( user === 404 ) {
                            this.setState( {exists: false} );
                            return;
                        }

                        api.getUserProfile( this.state.uid )
                            .then( res => {

                                if ( res.map.bio !== undefined ) {
                                    this.setState({bio: res.map.bio});
                                }

                                if ( res.map.profilePicture !== undefined ) {
                                    let url:string = res.map.profilePicture;
                                     url = "/" + url.slice(url.indexOf("data"));
                                     url = url.replaceAll("\\", "/");
                                    this.setState({profilePicture: url})
                                }

                                // THIS LINE BELOW IS CRITICAL
                                // Once you set exist to true, the userHeader component will render.
                                // If you try to set something after this, it won't work!

                                // return type is error if it is a number
                                if (typeof user !== "number") {
                                    this.setState({
                                        name: user.name,
                                        subscribers: res.map.subscribers,
                                        exists: true,
                                    });
                                }

                            } );

                        // api.getAllUserImages(localStorage.getItem("authToken"), this.state.uid);
                    })
            })
    }

    renderError() {
        return(
            <Center>
                <Text fontSize="4xl" color="red.500"> User does not exist! </Text>
            </Center>
        )
    }

    renderUser(){

        return (
            <>
                <UserHeader
                    uid={this.state.uid}
                    name={this.state.name}
                    hasOwnership={this.state.hasOwnership}
                    exists={this.state.exists}
                    bio={this.state.bio}
                    profilePicture={this.state.profilePicture}
                    subscribers={this.state.subscribers}
                />

                <UserFeed
                    uid={this.state.uid}
                    cuid={this.state.cuid}
                    hasOwnership={this.state.hasOwnership}
                />

            </>
        )
    }

    render() {

        switch( this.state.exists ) {

            case true:
                return this.renderUser();

            case false:
                return this.renderError();

            default:
                return ( <> </> );
        }
    }
}

export default UserPageWrapper;