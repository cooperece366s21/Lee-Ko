import {PostJson, CommentJson, UserObject, UserJson} from "./types";
export const BACKEND_URL = "http://localhost:5432";


// USER API CALLS
function getCurrentToken(): string {
    return localStorage.getItem("authToken") || "";
}

function setCurrentToken( token:string): void {
    localStorage.setItem("authToken", token);
}

export function logout() {
    localStorage.removeItem("authToken");
    window.location.replace("/");
}

export async function getCurrentUserID(): Promise<number> {
    let token = getCurrentToken();

    if (token === "") {
        return -1;
    }

    const res = await fetch(`${BACKEND_URL}/noauth/tokenToId`, {
        method: "get",
        mode: "cors",
        headers: {
            "Content-Type": "application/json",
            "token": token,
        },
    });

    if ( res.ok ) {

        return await res.json()
            .then(body => {
                return (JSON.parse(body).uid);
            });

    }

    else {
        return -1;
    }
}

export async function register(  email:string, username:string, password:string ) {

    const res = await fetch(`${BACKEND_URL}/noauth/register`, {
        method: "post",
        mode: "cors",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ email, username, password })
    });

    if ( res.ok ) {
        return 200;
    } else {
        // maybe some other code here for specific errors?
        return res.status;
    }

}

export async function login( email:string, password:string ) {

    // alert( JSON.stringify({ email, password } ) )

    const res = await fetch(`${BACKEND_URL}/noauth/login`, {
        method: "post",
        mode: "cors",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ email, password })
    });

    if ( res.ok ) {

        return await res.json()
            .then(body => {
                setCurrentToken(JSON.parse(body).authToken);
                return 200;
            });

    } else {
        // maybe some other code here for specific errors?
        return res.status;
    }
}

export async function getUser( uid:number ) {
    
    const res = await fetch(`${BACKEND_URL}/noauth/user/${uid}`, {
        headers: {
            "Content-Type": "application/json"
        },
    });

    if ( res.ok ) {

        return await res.json()
            .then(body => {
                let user:UserObject = body.map;
                user.avatar = convertToCorrectUrl(user.avatar);
                return user;
            });

    } else {
        return res.status;
    }
}

export async function searchUser( name:string ) {

    const res = await fetch( `${BACKEND_URL}/noauth/search/${name}`, {
        method: "get",
        mode: "cors",
        headers: {
            "Content-Type": "application/json"
        },
    });

    if (res.ok) {
        return await res.json()
            .then( body => {
                body = body.myArrayList;

                body.forEach( (user:UserJson, userIndex:number) => {
                    body[userIndex].map.avatar = convertToCorrectUrl(user.map.avatar);
                })

                return body;
            });
    }
    else {
        return 404;
    }
}

export async function editUser(token:string|null, name:string|null, bio:string|null ) {
    if ( token==null || name==null ) return (403);
    // JSON doesnt seem to like nulls being fields
    bio = bio==null ? "" : bio;

    const res = await fetch(`${BACKEND_URL}/auth/user/edit`, {
        method: "put",
        mode: "cors",
        headers: {
            "Content-Type": "application/json",
            "token": token,
        },
        body: JSON.stringify({ name, bio })
    });

    if ( res.ok ) {
        return 200;
    } else {
        // maybe some other code here for specific errors?
        alert( "Error: " + res.status);
        return res.status;
    }
}

function convertToCorrectUrl( dbUrl:string ) {
    dbUrl = dbUrl = "/" + dbUrl.slice(dbUrl.indexOf("data"));
    dbUrl = dbUrl.replaceAll("\\", "/");
    return dbUrl;
}

export async function getUserFeed( cuid:number, uid:number ) {

    const res = await fetch(`${BACKEND_URL}/auth/user/${uid}/feed`, {
        method: "get",
        mode: "cors",
        headers: {
            "Content-Type": "application/json",
            "token": getCurrentToken(),
        },
    });

    if( res.ok ) {
        return await res.json()
            .then( body => {

                body.forEach( (post:PostJson, postIndex:number) => {

                    post.map.images.forEach( (url:string, urlIndex ) => {
                        body[postIndex].map.images[urlIndex] = convertToCorrectUrl(url);
                    });

                    post.map.comments.forEach( (c:CommentJson, cIndex:number) => {
                        body[postIndex].map.comments[cIndex].map.avatar = convertToCorrectUrl(c.map.avatar)

                        c.map.children.forEach( ( cc:CommentJson, ccIndex:number) => {
                            body[postIndex].map.comments[cIndex].map.children[ccIndex].map.
                                avatar = convertToCorrectUrl(cc.map.avatar);
                        })
                    });

                    body[postIndex].map.comments.reverse();

                })

                body.reverse();
                return body;
            })
    } else {

    }


}

export async function getUserProfile( uid:number ) {


    const res = await fetch(`${BACKEND_URL}/noauth/user/${uid}/profile`, {
        headers: {
            "Content-Type": "application/json",
        },
    });

    if ( res.ok ) {

        return await res.json()
            .then(body => {
                return body;
            });

    } else {
        // maybe some other code here for specific errors?
        return res.status;
    }
}

// WALLET API CALLS

export async function getWallet( token:string|null ) {

    if ( token==null ) return (403);

    const res = await fetch(`${BACKEND_URL}/auth/wallet`, {
        method: "get",
        mode: "cors",
        headers: {
            "Content-Type": "application/json",
            "token": token,
        },
    });

    if ( res.ok ) {
        return await res.json()
            .then( body => {
                return JSON.parse(body);
            })

    } else{
        alert("ERROR: " + res.status );
    }

}

export async function addToWallet( token:string|null, amount:string|null ) {
    if (amount === ""){
        alert("No value inserted!");
        return;
    }

    if ( token==null ) return (403);

    const res = await fetch(`${BACKEND_URL}/auth/wallet/add`, {
        method: "post",
        mode: "cors",
        headers: {
            "Content-Type": "application/json",
            "token": token,
        },
        body: JSON.stringify({ amount } )
    });

    if ( res.ok ) {
        return 200;
    } else {
        alert(" Error adding money to wallet ");
        return res.status;
    }
}

// POST API CALLS
function formPost( title:string, contents:string, pictures:File[]|null, tier:number ) {
    let form = new FormData();
    let numberOfFiles = pictures==null ? "0" : pictures?.length.toString();

    form.set('title', title);
    form.set('contents', contents);
    form.set('numberOfImages', numberOfFiles);
    form.set('tier', tier.toString());

    let i = 0;
    pictures?.forEach( file => {
        form.set('image'+i, file);
        form.set('imageType'+i, "." + file.type.slice( file.type.indexOf("/")+1 ));
        i++;
    })

    return form;
}

export async function createPost( token:string|null, title:string, contents:string, pictures:File[]|null, tier:number|null) {

    if ( token==null ) return (403);
    if ( tier==null ) {
        return 404;
    }

    let numberOfFiles = pictures==null ? "0" : pictures?.length.toString();

    let form = formPost( title, contents, pictures, tier);

    const res = await fetch(`${BACKEND_URL}/auth/post`, {
        method: "post",
        mode: "cors",
        headers: {
            "enctype": "multipart/form-data",
            "token": token,
        },
        body: form,
    });

    if ( res.ok ) {
        return 200;
    } else {
        // maybe some other code here for specific errors?
        return res.status;
    }

}

export async function getPost( token:string|null, pid:number ) {
    if ( token==null ) return (403);

    const res = await fetch(`${BACKEND_URL}/auth/post/${pid}`, {
        method: "get",
        mode: "cors",
        headers: {
            "Content-Type": "application/json",
            "token": token,
        },
    });

    if ( res.ok ) {
        return await res.json()
            .then( body => {

                body.map.urls.forEach( (url:string, i:number) => {
                    body.map.urls[i] = convertToCorrectUrl(url);
                });

                return body.map;
            })

    } else{
        alert("ERROR: " + res.status );
    }

}

export async function editPost( pid:string, title:string, contents:string, pictures:File[]|null, tier:number|null, deleted:number[] ) {
    if ( tier== null ) return (404);

    let form = formPost( title, contents, pictures, tier);
    form.set("iidToDelete", deleted.toString());

    const res = await fetch(`${BACKEND_URL}/auth/post/${pid}`, {
        method: "put",
        mode: "cors",
        headers: {
            "enctype": "multipart/form-data",
            "token": getCurrentToken(),
        },
        body: form,
    });

    if ( res.ok ) {
        return 200;
    } else {
        // maybe some other code here for specific errors?
        alert(res.status);
        return res.status;
    }
}

export async function deletePost(pid:number ) {

    const res = await fetch(`${BACKEND_URL}/auth/post/${pid}`, {
        method: "delete",
        mode: "cors",
        headers: {
            "Content-Type": "application/json",
            "token": getCurrentToken(),
        },
    });

    if ( res.ok ) {
        return 200;
    } else{
        return res.status;
    }
}

export async function addLike( token:string|null, pid:number ) {

    if ( token==null ) return (403);

    const res = await fetch(`${BACKEND_URL}/auth/post/${pid}/like`, {
        method: "post",
        mode: "cors",
        headers: {
            "Content-Type": "application/json",
            "token": token,
        },
        body: JSON.stringify({ token })
    });

    if (res.ok) {
        return 200;
    } else {
        alert("ERROR LIKING POST: " + res.status);
        return res.status;
    }


}

export async function getLikes( token: string|null, pid: number ) {

    if ( token==null ) return (403);

    const res = await fetch(`${BACKEND_URL}/auth/post/${pid}/like`, {
        method: "get",
        mode: "cors",
        headers: {
            "Content-Type": "application/json",
            "token" : token,
        },
        body: JSON.stringify({ token })
    });

}

// COMMENT API CALLS

export async function makeComment( token: string|null, pid:number, contents:string, parentId:number|null) {
    if ( token==null ) return (403);
    if ( contents==="" ) return (404);
    parentId = parentId==null ? -1 : parentId;

    const res = await fetch(`${BACKEND_URL}/auth/post/${pid}/comment`, {
        method: "post",
        mode: "cors",
        headers: {
            "Content-Type": "application/json",
            "token": token,
        },
        body: JSON.stringify({ contents, parentId })
    });

    if (res.ok) {
        return 200;
    } else {
        alert("ERROR :" +res.status);
        return res.status;
    }
}



// IMAGE API CALLS

export async function uploadProfilePicture( token:string|null, file:File|null) {
    if ( token==null ) return (403);
    if ( file==null ) return (403);

    const form = new FormData();
    form.append( "file", file );

    let fileType = "." + file.type.slice( file.type.indexOf("/")+1 );
    form.append( "fileType", fileType );

    const res = await fetch(`${BACKEND_URL}/auth/user/profilepic`, {
        method: "post",
        mode: "cors",
        headers: {
            "enctype": "multipart/form-data",
            "token": token,
        },
        body: form,
    });

    if (res.ok){
        return 200;
    } else{
        return res.status;
    }

}

export async function getAllUserImages( token:string|null, uid:number ) {
    if ( token==null ) return (403);

    const res = await fetch(`${BACKEND_URL}/images/${uid}/`, {
        method: "get",
        mode: "cors",
        headers: {
            "Content-Type": "application/json",
            "token" : token,
        },
    });

    if (res.ok) {
        res.json()
            // .then( json => alert( JSON.stringify(json) ) )
        return 200;
    } else {
        alert( res.status );
    }
}

// SUBSCRIBER API CALLS

export async function getSubscribers ( uid:number ) {
    const res = await fetch(`${BACKEND_URL}/noauth/user/${uid}/subscriptions`, {
        method: "get",
        mode: "cors",
        headers: {
            "Content-Type": "application/json",
        },
    });

    res.json()
        .then( body => {
            alert( JSON.stringify(body) );
        } )
}

export async function subscribeTo( uid:number, tier:number ) {

    const res = await fetch(`${BACKEND_URL}/auth/user/${uid}/subscriptions`, {
        method: "post",
        mode: "cors",
        headers: {
            "Content-Type": "application/json",
            "token": getCurrentToken(),
        },
        body: JSON.stringify( { tier } )
    });

    if (res.ok) {
        return 200;
    } else {
        return res.status;
    }

}


let exports = {
    register,
    login,
    logout,
    getCurrentUserID,
    getUser,
    searchUser,

    getUserProfile,
    editUser: editUser,
    getSubscribers,
    getUserFeed,

    createPost,
    editPost,
    getPost,
    deletePost,

    makeComment,

    uploadProfilePicture,
    getAllUserImages,

    getWallet,
    addToWallet,

    addLike,
    getLikes,

    subscribeTo,
}

export default exports
