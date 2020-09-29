package com.taskforce.superinvention.app.web.dto.role

import com.taskforce.superinvention.app.domain.role.Role

class RoleDto (
        var name: Role.RoleName,
        var roleGroupName: String
) {
        constructor(role: Role) :
                this(
                        name = role.name,
                        roleGroupName = role.roleGroup.name
                )
}